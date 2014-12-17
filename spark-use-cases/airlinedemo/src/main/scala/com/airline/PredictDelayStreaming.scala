package com.airline
import org.apache.spark.rdd._
import scala.collection.JavaConverters._
import au.com.bytecode.opencsv.CSVReader
import java.io._
import org.joda.time._
import org.joda.time.format._
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.mllib.classification.{ NaiveBayes, NaiveBayesModel }
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.classification.SVMWithSGD
import org.apache.hadoop.fs.FileSystem
import org.apache.hadoop.fs.Path
import org.apache.spark.streaming.{Seconds, StreamingContext}
import java.io.ObjectInputStream
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.mllib.classification.SVMModel
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat
import scala.collection.mutable.ListBuffer
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.Date
import org.apache.spark.AccumulatorParam

// cd / && rm -rf airline-demo/ && cp -r /code/spark-demo/airline-demo/ /airline-demo && cd airline-demo/ && mvn install
// hadoop fs -copyFromLocal airline2007.csv.bz2 .
//
//HERE
//  

/*
 * 
 */



//object iListBuffer extends AccumulatorParam[ListBuffer[String]] {
//  def zero(m: ListBuffer[String]) = ListBuffer()
//  def addInPlace(m1: ListBuffer[String], m2: ListBuffer[String]) = m1 ++ m2 
//}



object PredictDelayStreaming {

   val holidays = List("01/01/2007", "01/15/2007", "02/19/2007", "05/28/2007", "06/07/2007", "07/04/2007",
    "09/03/2007", "10/08/2007", "11/11/2007", "11/22/2007", "12/25/2007",
    "01/01/2008", "01/21/2008", "02/18/2008", "05/22/2008", "05/26/2008", "07/04/2008",
    "09/01/2008", "10/13/2008", "11/11/2008", "11/27/2008", "12/25/2008")
  
  def get_hour(depTime: String): String = "%04d".format(depTime.toInt).take(2)

  def days_from_nearest_holiday(year: Int, month: Int, day: Int): Int = {
    val sampleDate = new DateTime(year, month, day, 0, 0)

    holidays.foldLeft(3000) { (r, c) =>
      val holiday = DateTimeFormat.forPattern("MM/dd/yyyy").parseDateTime(c)
      val distance = Math.abs(Days.daysBetween(holiday, sampleDate).getDays)
      math.min(r, distance)
    }
  }
    
    
  // function to do a preprocessing step for a given file
  def prepFlightDelays(inputDStream: InputDStream[(LongWritable,Text)]): DStream[(Text,Array[Double])] = {
   

    // field 21 is cancelled field
    // cat /GIT/af/spark-demo/airline-demo/src/main/resources/airline2007.csv | cut -d, -f 22
    // 1 	Year 	1987-2008
    // 2 	Month 	1-12
    // 3 	DayofMonth 	1-31
    // 4 	DayOfWeek 	1 (Monday) - 7 (Sunday)
    // 6 	CRSDepTime 	scheduled departure time (local, hhmm)
    // 16 	DepDelay 	departure delay, in minutes
    // 17 	Origin 	origin IATA airport code
    // 19 	Distance 	in miles
    // 22 	Cancelled 	was the flight cancelled?   0 no, 1 yes
    
    inputDStream.map { tuple =>
      
      val reader = new CSVReader(new StringReader(tuple._2.toString()))
      val rec = reader.readAll().get(0)
      val year = rec(0)
      val month = rec(1)
      val dayofmonth = rec(2)
      val dayofweek = rec(3)
      val crsdeptime = rec(5)
      val depdelay = rec(15)
      val origin = rec(16)
      val distance = rec(18)
      val cancelled = rec(21)
      
      val flightnb = rec(9)
      val destination = rec(17)
      val FlightDescription = new Text("Flight Number " + flightnb + ", from " + origin + ", destination = " + destination)
      
      if (!year.equals("Year") && cancelled.equals("0")){
         (FlightDescription,Array(
          depdelay.toDouble,
          month.toDouble,
          dayofmonth.toDouble,
          dayofweek.toDouble,
          get_hour(crsdeptime).toDouble,
          distance.toDouble,
          days_from_nearest_holiday(year.toInt, month.toInt, dayofmonth.toInt)))
      }
      else
      {
         // they will be filtered next
         (new Text("Wrong"), Array(
          -1.toDouble,
          -1.toDouble,
          -1.toDouble,
          -1.toDouble,
          -1.toDouble,
          -1.toDouble,
          -1.toDouble))
      }  
      
    }.filter(rec => !(rec._1.equals("Wrong")))
      

  }

  def labelDatasetPoints(vals: (Text,Array[Double])): (Text,LabeledPoint) = {
    // here we label points
    // if delay > 15 min label = 1
    //              else label = 0
    (vals._1, LabeledPoint(if (vals._2(0) >= 15) 1.0 else 0.0, Vectors.dense(vals._2.drop(1))))
  }

 
  
  // main method
  def main(args: Array[String]) {
    
    val conf = new SparkConf().setAppName("Streaming: ml determine if flight is delayed")
    var monitoredDir = "" 
    var modelPath=""
    var outPath=""  
    
    // retrieve input arguments
    // when called from eclipse no arguments
    // when called from Spark, expect two arguments
    if (args.length == 0) {
       // we provide to run in eclipse 
       monitoredDir =  "src/main/resources/monitoreddir"
       modelPath = "target/airline.model"
       outPath="target/output"  
       conf.setMaster("local")
    }   
    else { 
       monitoredDir =  args(0)
       modelPath=args(1)
       outPath=args(2)
    }
      
    // read model file from hdfs: this ml model was build from class BuildAirlineDelay
    val hadoopConf = new org.apache.hadoop.conf.Configuration() //get's default configuration
    val fs=FileSystem.get(hadoopConf)
    val iis = new ObjectInputStream(fs.open(new Path(modelPath)))
    val delaySVMModel = iis.readObject().asInstanceOf[SVMModel] 
    
    
    // Now create a Spark Streaming context: read monitored folder every seconds
    val ssc = new StreamingContext(conf, Seconds(10))
    
    // create distributed counters in order to count the
    // total number of Good and Bad predictions
    val goodPredicAccum = ssc.sparkContext.accumulator(0, "Good Prediction")
    val badPredicAccum = ssc.sparkContext.accumulator(0, "Bad Prediction")
    
    // broadcast output path: it needs to be sent to executors
    val broadcastOutputPath = ssc.sparkContext.broadcast(outPath)
    
    // broadcast model
   val broadcastDelaySVMModel = ssc.sparkContext.broadcast(delaySVMModel)
  
   
    // monitor hdfs directory
    val sscFileStream = ssc.fileStream[LongWritable, Text, TextInputFormat](directory = monitoredDir,filter = (x:Path) => true, newFilesOnly = false ) 
    
    /*
     * DATA PREPARATION PHASE
     */
    
    //  array of double (depdelay,month, dayofmonth, dayofweek, hour, distance, daysfromnearestholiday)        
    val parsedInputStream = prepFlightDelays(sscFileStream) 
    
    
    // label points: if delay > 15 label=1 else label =0
    val parsedInputStreamLabelled = parsedInputStream.map(labelDatasetPoints)
    
    
    // now do the prediction on the fly for delays 
     parsedInputStreamLabelled.foreachRDD(rdd => {                  
               if (rdd.count() != 0) 
               {
                  val scaler = new StandardScaler(withMean = true, withStd = true).fit(rdd.map(x => x._2.features))               
                  val parsedInputStreamLabelledScaled = rdd.map(x => (x._1,LabeledPoint(x._2.label, scaler.transform(Vectors.dense(x._2.features.toArray)))))
                  val predictionRDD = parsedInputStreamLabelledScaled.map(elem => {
                     val pred = broadcastDelaySVMModel.value.predict(elem._2.features)
                     
                     if (pred.equals(elem._2.label)){
                      goodPredicAccum.add(1)
                     }
                     else {
                      badPredicAccum.add(1)
                     }  
                     elem._1 + ":prediction for delay= " + pred + ", actual delay=" + elem._2.label
                  })
 
                  predictionRDD.saveAsTextFile(broadcastOutputPath.value + "/" + InetAddress.getLocalHost().getHostName() + new SimpleDateFormat("yyyy-MM-dd_hh-mm-ss").format(new Date()))
               }
    })
   


    
    println("Starting Streaming")
    ssc.start()
    ssc.awaitTermination()
   
    
  }
}

