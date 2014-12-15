package com.bigdatawave.bookingsanalysis

import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD

import org.apache.log4j.Logger
import org.apache.log4j.Level

object WordcountStreaming {
  def main(args: Array[String]) {
    
     // Uncomment the 2 lines below when running the code under eclipse
    // in order to get rid of all the logs and focus only on the standard output

    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)

    
    var hostname:String = ""
    var port:Int = 0
    var timeWindow:Int = 0
    val conf = new SparkConf().setAppName("WordcountStreaming")
    
    println(args.mkString(","))
    // If the program is run within eclipse
    if (args.length == 0) {
      hostname = "<put your IP adress here>"
      port = 9999
      timeWindow = 60
      conf.setMaster("local[2]")
    } else if (args.length < 3) {
      println("Error missing arguments. Usage : " + this.getClass().getCanonicalName() + " <hostname> <port> <timeWindow(seconds)>")
      System.exit(1)
    } else {
      // Get parameters from arguments
      hostname = args(0)
      port = args(1).toInt
      timeWindow = args(2).toInt
          // Create a local StreamingContext with two working thread and batch interval of 1 second
	    conf.set("spark.akka.threads", "4")
    }

    
    val ssc = new StreamingContext(conf, Seconds(timeWindow))
    
    // Create a DStream that will connect to hostname:port, like localhost:9999
    val lines = ssc.socketTextStream(hostname, port)

    // Split each line into words
    val words = lines.flatMap(_.split(","))

    // Count each word in each batch
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)
    //val test = ssc.sparkContext.makeRDD( List("Debug : Running",System.currentTimeMillis(),wordCounts.foreachRDD( r => r.toString )))
    //test.saveAsTextFile("hdfs:///user/root/test.out")
    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print()

    ssc.start() // Start the computation
    ssc.awaitTermination() // Wait for the computation to terminate

  }

}