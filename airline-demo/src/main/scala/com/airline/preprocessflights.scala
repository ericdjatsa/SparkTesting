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
import org.apache.spark.mllib.classification.{NaiveBayes, NaiveBayesModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.LabeledPoint

import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.classification.SVMWithSGD


// cd / && rm -rf airline-demo/ && cp -r /code/spark-demo/airline-demo/ /airline-demo && cd airline-demo/ && mvn install
// hadoop fs -copyFromLocal airline2007.csv.bz2 .

object PreProcessFlights  {
   

  // function to do a preprocessing step for a given file
  def prepFlightDelays(sc: SparkContext, infile: String): RDD[DelayRec] = {
    val data = sc.textFile(infile)

    data.map { line =>
      val reader = new CSVReader(new StringReader(line))
      reader.readAll().asScala.toList.map(rec => DelayRec(rec(0),rec(1),rec(2),rec(3),rec(5),rec(15),rec(16),rec(18),rec(21)))
    }.map(list => list(0))
    .filter(rec => rec.year != "Year")
    .filter(rec => rec.cancelled == "0")
    .filter(rec => rec.origin == "ORD")
  }

 def parseData(vals: Array[Double]): LabeledPoint = {
    LabeledPoint(if (vals(0)>=15) 1.0 else 0.0, Vectors.dense(vals.drop(1)))
 }

 // Function to compute evaluation metrics
def eval_metrics(labelsAndPreds: RDD[(Double, Double)]) : Tuple2[Array[Double], Array[Double]] = {
    val tp = labelsAndPreds.filter(r => r._1==1 && r._2==1).count.toDouble
    val tn = labelsAndPreds.filter(r => r._1==0 && r._2==0).count.toDouble
    val fp = labelsAndPreds.filter(r => r._1==1 && r._2==0).count.toDouble
    val fn = labelsAndPreds.filter(r => r._1==0 && r._2==1).count.toDouble

    val precision = tp / (tp+fp)
    val recall = tp / (tp+fn)
    val F_measure = 2*precision*recall / (precision+recall)
    val accuracy = (tp+tn) / (tp+tn+fp+fn)
    new Tuple2(Array(tp, tn, fp, fn), Array(precision, recall, F_measure, accuracy))
}


  // main method
   def main(args: Array[String]) { 
  // Create a Spark conf
  val conf = new SparkConf().setAppName("Preprocessing Flights")
    
  // Now create a Spark context
  val sc = new SparkContext(conf)
    
  val outputPath = args(0)


  val data_2007 = prepFlightDelays(sc, "hdfs://hadoopmaster.example.com:8020//user/root/airline2007.csv.bz2").map(rec => rec.gen_features._2)
  val data_2008 = prepFlightDelays(sc, "hdfs://hadoopmaster.example.com:8020/user/root/airline2008.csv.bz2").map(rec => rec.gen_features._2)
  //data_2007.take(5).map(x => x mkString ",").foreach(println)

  // Prepare training set
  val parsedTrainData = data_2007.map(parseData)
  parsedTrainData.cache
  val scaler = new StandardScaler(withMean = true, withStd = true).fit(parsedTrainData.map(x => x.features))
  val scaledTrainData = parsedTrainData.map(x => LabeledPoint(x.label, scaler.transform(Vectors.dense(x.features.toArray))))
  scaledTrainData.cache

  // Prepare test/validation set
  val parsedTestData = data_2008.map(parseData)
  parsedTestData.cache
  val scaledTestData = parsedTestData.map(x => LabeledPoint(x.label, scaler.transform(Vectors.dense(x.features.toArray))))
  scaledTestData.cache

  // Build the SVM model
  val svmAlg = new SVMWithSGD()
  svmAlg.optimizer.setNumIterations(100)
                .setRegParam(1.0)
                .setStepSize(1.0)
  val model_svm = svmAlg.run(scaledTrainData)

  // Predict
  val labelsAndPreds_svm = scaledTestData.map { point =>
        val pred = model_svm.predict(point.features)
        (pred, point.label)
  }
  val m_svm = eval_metrics(labelsAndPreds_svm)._2
  println("precision = %.2f, recall = %.2f, F1 = %.2f, accuracy = %.2f".format(m_svm(0), m_svm(1), m_svm(2), m_svm(3)))

 }
}

case class DelayRec(year: String,
                    month: String,
                    dayOfMonth: String,
                    dayOfWeek: String,
                    crsDepTime: String,
                    depDelay: String,
                    origin: String,
                    distance: String,
                    cancelled: String) {

    val holidays = List("01/01/2007", "01/15/2007", "02/19/2007", "05/28/2007", "06/07/2007", "07/04/2007",
      "09/03/2007", "10/08/2007" ,"11/11/2007", "11/22/2007", "12/25/2007",
      "01/01/2008", "01/21/2008", "02/18/2008", "05/22/2008", "05/26/2008", "07/04/2008",
      "09/01/2008", "10/13/2008" ,"11/11/2008", "11/27/2008", "12/25/2008")

    def gen_features: (String, Array[Double]) = {
      val values = Array(
        depDelay.toDouble,
        month.toDouble,
        dayOfMonth.toDouble,
        dayOfWeek.toDouble,
        get_hour(crsDepTime).toDouble,
        distance.toDouble,
        days_from_nearest_holiday(year.toInt, month.toInt, dayOfMonth.toInt)
      )
      new Tuple2(to_date(year.toInt, month.toInt, dayOfMonth.toInt), values)
    }

    def get_hour(depTime: String) : String = "%04d".format(depTime.toInt).take(2)
    def to_date(year: Int, month: Int, day: Int) = "%04d%02d%02d".format(year, month, day)

    def days_from_nearest_holiday(year:Int, month:Int, day:Int): Int = {
      val sampleDate = new DateTime(year, month, day, 0, 0)

      holidays.foldLeft(3000) { (r, c) =>
        val holiday = DateTimeFormat.forPattern("MM/dd/yyyy").parseDateTime(c)
        val distance = Math.abs(Days.daysBetween(holiday, sampleDate).getDays)
        math.min(r, distance)
      }
    }
  }