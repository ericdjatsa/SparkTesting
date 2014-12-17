package com.bigdatawave.bookingsanalysis
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.streaming._
import org.apache.spark.SparkConf
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql._

import org.apache.log4j.Logger
import org.apache.log4j.Level

// Create an adhoc case class for mapping airports
case class Airport(airportcode : String, airportname : String,  city : String, country : String)

object BookingAnalysisStreaming {
  def main(args: Array[String]) {
    // Check parameters
    if (args.length < 3) {
      println("Error missing arguments. Usage : " + this.getClass().getCanonicalName() + " <hostname> <port> <timeWindow(seconds)>")
      System.exit(1)
    }
    
    // Uncomment the 2 lines below for when running the code under eclipse
    // in order to get rid of all the logs and focus only on the standard output
   
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)
    
    
	// Get parameters from arguments
	val hostname = args(0)
	val port = args(1).toInt
	val timeWindow = args(2).toInt
	
	val conf = new SparkConf().setAppName("BookingAnalysisStreaming")
	
	// Uncomment the line below when testing under eclipse
	conf.setMaster("local[2]")
	
	// Now create a Spark context
    val sc = new SparkContext(conf)
	
	//hiveSqlContext.sql("LOAD DATA LOCAL INPATH 'src/main/resources/cities_data.txt' INTO TABLE Cities")
	
	// Get airports -> city mappings
    val airports_data = sc.textFile("src/main/resources/airports_data.txt")
    
    val airports = airports_data.
    				map(_.split(",")).
    				    map(fields => Airport(fields(0),fields(1),fields(2), fields(3)))
    				        
        // Create a SparkSQL context
    val sqlContext = new SQLContext(sc)
    
    // Import implicit register methods from the sqlContext object
    import sqlContext._
    
    // Register the airports as a Spark SQL table
    airports.registerAsTable("airports")				        
	
	// get airport -> City mappings
	val airport_city = sqlContext.sql("""select airportcode, city
												  from airports """).map(row => (row.getString(0), row.getString(1)))
	// cache the retrieved data										  
	airport_city.cache											  
	val airport_city_mapping = airport_city.collectAsMap
	
	// Now create a streaming context for processing live data
	val ssc = new StreamingContext(sc, Seconds(timeWindow))
	
    // Create a DStream that will connect to hostname:port, like localhost:9999
    val in_data = ssc.socketTextStream(hostname, port)
    
    import org.apache.spark.streaming.StreamingContext._
	// Parse the lines and create Booking records
    val bookings = in_data.
    				map(_.split(",")).
    				    map(fields => BookingRecord(fields(0),fields(1),fields(2),fields(3),fields(4),
    				    							fields(5),fields(6),fields(7),fields(8),
    				    							fields(9),fields(10).trim.toInt,fields(11).trim.toFloat))
    // build (key,value) : ((depAirport, destAirport) , 1 )				    							
    val airport_pairs = bookings.map(br => ((br.departureAirport, br.destinationAirport),1))
    
    val airportpairsCount = airport_pairs.reduceByKey(_ + _)
    
	val output = airportpairsCount.map(airportPairCount => 
	  { val airport1 = (airportPairCount._1)._1
	    val city1 = airport_city_mapping.getOrElse(airport1, "Unknown")
	    val airport2 = (airportPairCount._1)._2
	    val city2 = airport_city_mapping.getOrElse(airport2, "Unknown")
	    val count:Int = airportPairCount._2
	    
	    ((airport1,city1,airport2,city2),count)
	  })
	  
	  output.print()
	  
      ssc.start() // Start the computation
      ssc.awaitTermination() // Wait for the computation to terminate

  }

}