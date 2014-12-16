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


// cd / && rm -rf airline-demo/ && cp -r /code/spark-demo/airline-demo/ /airline-demo && cd airline-demo/ && mvn install
// hadoop fs -copyFromLocal airline2007.csv.bz2 .

object BuildAirlineDelayModel {

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
    
    
 
  def prepFlightDelays(sc: SparkContext, infile: String): RDD[Array[Double]] = {
    val data = sc.textFile(infile)

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
    
    data.map { line =>
      val reader = new CSVReader(new StringReader(line))
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
      
      if (!year.equals("Year") && cancelled.equals("0")){
         Array(
          depdelay.toDouble,
          month.toDouble,
          dayofmonth.toDouble,
          dayofweek.toDouble,
          get_hour(crsdeptime).toDouble,
          distance.toDouble,
          days_from_nearest_holiday(year.toInt, month.toInt, dayofmonth.toInt))
      }
      else
      {
         // they will be filtered next
         Array(
          -1.toDouble,
          -1.toDouble,
          -1.toDouble,
          -1.toDouble,
          -1.toDouble,
          -1.toDouble,
          -1.toDouble)
      }  
      
    }.filter(rec => !(rec(0).equals(-1.toDouble)))
      

  }

  def labelDatasetPoints(vals: Array[Double]): LabeledPoint = {
    // here we label points
    // if delay > 15 label = 1
    //          else label =0
    LabeledPoint(if (vals(0) >= 15) 1.0 else 0.0, Vectors.dense(vals.drop(1)))
  }

  // Function to compute evaluation metrics
  def eval_metrics(labelsAndPreds: RDD[(Double, Double)]): Tuple2[Array[Double], Array[Double]] = {
    val tp = labelsAndPreds.filter(r => r._1 == 1 && r._2 == 1).count.toDouble
    val tn = labelsAndPreds.filter(r => r._1 == 0 && r._2 == 0).count.toDouble
    val fp = labelsAndPreds.filter(r => r._1 == 1 && r._2 == 0).count.toDouble
    val fn = labelsAndPreds.filter(r => r._1 == 0 && r._2 == 1).count.toDouble

    val precision = tp / (tp + fp)
    val recall = tp / (tp + fn)
    val F_measure = 2 * precision * recall / (precision + recall)
    val accuracy = (tp + tn) / (tp + tn + fp + fn)
    new Tuple2(Array(tp, tn, fp, fn), Array(precision, recall, F_measure, accuracy))
  }

  
  
  // main method
  def main(args: Array[String]) {
    
    val conf = new SparkConf().setAppName("Preprocessing Flights")
    var inputFile = "" 
    var modelPath=""
    
    // retrieve input arguments
    // when called from eclipse no arguments
    // when called from Spark, expect one argument
    if (args.length == 0) {
       // we provide to run in eclipse 
       inputFile =  "src/main/resources/modeldata/airline2007.csv"
       modelPath = "target/airline.model"
       conf.setMaster("local")
    }   
    else { 
       inputFile=args(0)
       modelPath=args(1)
    }
      
    // Now create a Spark context
    val sc = new SparkContext(conf)

    /*
     * DATA PREPARATION PHASE
     */
    
    //  array of double (depdelay,month, dayofmonth, dayofweek, hour, distance, daysfromnearestholiday)        
    val data_2007 = prepFlightDelays(sc, inputFile) 
    data_2007.take(5).map(x => x mkString ",").foreach(println)
    
    // label points: if delay > 15 label=1 else label =0
    val data_2007_labelled = data_2007.map(labelDatasetPoints)
    data_2007_labelled.cache
    
    // now standardize metrics
    val scaler = new StandardScaler(withMean = true, withStd = true).fit(data_2007_labelled.map(x => x.features))
    val data_2007_labelled_scaled = data_2007_labelled.map(x => LabeledPoint(x.label, scaler.transform(Vectors.dense(x.features.toArray))))
    data_2007_labelled_scaled.cache
    
    // Split data into training (60%) and test (40%).
    val splits = data_2007_labelled_scaled.randomSplit(Array(0.6, 0.4), seed = 11L)
    val training = splits(0)
    val test = splits(1)
    
     // Build the SVM model
    val svmAlg = new SVMWithSGD()
    
    svmAlg.optimizer.setNumIterations(100)
      .setRegParam(1.0)
      .setStepSize(1.0)
    val model_svm = svmAlg.run(training)
    
    
    // Calculate Precision with Test Data
    val labelsAndPreds_svm = test.map { point =>
      val pred = model_svm.predict(point.features)
      (pred, point.label)
    }
    val m_svm = eval_metrics(labelsAndPreds_svm)._2
    println("precision = %.2f, recall = %.2f, F1 = %.2f, accuracy = %.2f".format(m_svm(0), m_svm(1), m_svm(2), m_svm(3)))

    // save model to hdfs
    val hadoopConf = new org.apache.hadoop.conf.Configuration() //get's default configuration
    val fs=FileSystem.get(hadoopConf);
    val outputStream=fs.create(new Path(modelPath))
    val oos=new ObjectOutputStream(outputStream)  
    oos.writeObject(model_svm)
    oos.close 
    
    
   
    
  }
}

