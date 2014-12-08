package com.bigdatawave.bookingsanalysis

import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.sql._

object BookingAnalysisSQL {
  def main(args: Array[String]) {
    // Create a Spark conf
    val conf = new SparkConf().setAppName("Bookings Analysis SQL")
    
    // Now create a Spark context
    val sc = new SparkContext(conf)
    
    // Get output path parameter
    val outputPath = args(1)
    
    
    // Lines have the following fields
    // BookingID, BookingTimestamp , CarrierCode, FlightID, CustomerID, 
    // AgeCategory , DepartureCity, DestinationCity, DepartureDate, ReturnDate, NbPassengers , TicketPrice
    
    // Get the lines of the input file
    val rawlines = sc.textFile(args(0))
    
    // Parse the lines and create Booking records
    val bookings = rawlines.
    				map(_.split(",")).
    				    map(fields => BookingRecord(fields(0),fields(1),fields(2),fields(3),fields(4),
    				    							fields(5),fields(6),fields(7),fields(8),
    				    							fields(9),fields(10).trim.toInt,fields(11).trim.toFloat))
    
    // Create a SparkSQL context
    val sqlContext = new SQLContext(sc)
    
    // Import implicit register methods from the sqlContext object
    import sqlContext._
    
    // Register the BookingRecords as a Spark SQL table
    bookings.registerAsTable("Bookings")

	val citypairsCountOrdered = sqlContext.sql("""select departureCity, destinationCity,count(*) as numbookings 
												  from Bookings 
												  group by departureCity,destinationCity
												  order by numbookings desc""")
												  
    // Save output to a textFile
    citypairsCountOrdered.saveAsTextFile(outputPath)
    
  }

}