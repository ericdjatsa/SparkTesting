/**
Booking Demo on Spark
 */

package com.bigdatawave.bookingsanalysis

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf

object BookingAnalysis {
  def main(args: Array[String]) {
    
    // Create a Spark conf
    val conf = new SparkConf().setAppName("Bookings Analysis")
    
    // Now create a Spark context
    val sc = new SparkContext(conf)
    
    val outputPath = args(1)
    
    // Lines have the following fields
    // BookingID, BookingTimestamp , CarrierCode, FlightID, CustomerID, 
    // AgeCategory , departureAirport, destinationAirport, DepartureDate, ReturnDate, NbPassengers , TicketPrice
    
    // Get the lines of the input file
    val rawlines = sc.textFile(args(0))
    
    // Parse the lines and create Booking records
    val bookings = rawlines.
    				map(_.split(",")).
    				    map(fields => BookingRecord(fields(0),fields(1),fields(2),fields(3),fields(4),
    				    							fields(5),fields(6),fields(7),fields(8),
    				    							fields(9),fields(10).trim.toInt,fields(11).trim.toFloat))
    
    				    							
    // Group the bookings by departureAirport and destination airport then count the occurrences for each combination
    val airportpairsCount = bookings.groupBy(booking => (booking.departureAirport,booking.destinationAirport)).mapValues(_.size)				    							
    
    // Sort the resulting map { (airport1,airport2) -> count} by count value
    val airportpairsCountOrdered = airportpairsCount.sortBy(_._2 , ascending=false, numPartitions=4)
    
    // More readable alternative for sorting is :  Swap keys and values for sorting purpose, then apply sorting on count
    // val airportpairsCountOrdered = (airportpairsCount.map{ case ((airport1,airport2),count) => (count,(airport1,airport2)) }).sortByKey(ascending=false)
    
    // Save output to a textFile
    airportpairsCountOrdered.saveAsTextFile(outputPath)
    
    //System.out.println(airportpairsCount.collect().mkString(", "))
  }
}
