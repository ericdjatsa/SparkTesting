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
import java.io.{File,FileInputStream,FileOutputStream}
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat

// cd / && rm -rf airline-demo/ && cp -r /code/spark-demo/airline-demo/ /airline-demo && cd airline-demo/ && mvn install
// hadoop fs -copyFromLocal airline2007.csv.bz2 .

object Test {

 
  
  // main method
  def main(args: Array[String]) {
    
    val conf = new SparkConf().setAppName("Streaming: ml determine if flight is delayed")
    var monitoredDir = "" 
    var modelPath=""
    
    // retrieve input arguments
    // when called from eclipse no arguments
    // when called from Spark, expect one argument
    if (args.length == 0) {
       // we provide to run in eclipse 
       monitoredDir =  "src/main/resources/monitoreddir/"
      
         
       modelPath = "target/airline.model"
       conf.setMaster("local")
    }   
    else { 
       monitoredDir =  args(0)
       modelPath=args(1)
    }
      
    // Now create a Spark Streaming context: read monitored folder every seconds
    val ssc = new StreamingContext(conf, Seconds(10))
    
    //val kiki = ssc.fileStream[LongWritable, Text, TextInputFormat](directory = monitoredDir, filter = (x:Path) => true, newFilesOnly = false)
    val kiki = ssc.fileStream[LongWritable, Text, TextInputFormat](directory = monitoredDir,filter = (x:Path) => true, newFilesOnly = false ) 
 
    
    
    // monitor hdfs directory
    //val sscFileStream = ssc.textFileStream(monitoredDir)
    //val kiki = ssc.fileStream(monitoredDir)
    val coco = kiki.map(v => v + "coco")
    coco.print()
    //sscFileStream.print()
   
    println("Starting Streaming")
    ssc.start()
    
     
    
    ssc.awaitTermination()
   
    
  }
}

