package com.bigdatawave.bookingsanalysis



object testBookings {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  val lines = List("1,20141204-103000,AF,153,2145,SENIOR,NCE,CDG,20141220,20141231,2,150",
  								 "2,20141204-103005,AF,124,1462,ADULT,LYS,MRS,20141215,20150105,1,115",
  								 "3,20141204-103005,AF,147,1462,ADULT,LYO,MAR,20141215,20150105,1,130")
                                                  //> lines  : List[String] = List(1,20141204-103000,AF,153,2145,SENIOR,NCE,CDG,20
                                                  //| 141220,20141231,2,150, 2,20141204-103005,AF,124,1462,ADULT,LYS,MRS,20141215,
                                                  //| 20150105,1,115, 3,20141204-103005,AF,147,1462,ADULT,LYO,MAR,20141215,2015010
                                                  //| 5,1,130)
  val a = lines.map(_.split(",")).map(array => array(2))
                                                  //> a  : List[String] = List(AF, AF, AF)
  val bookings = lines.
    				map(_.split(",")).
    				    map(fields => BookingRecord(fields(0),fields(1),fields(2),fields(3),fields(4),
    				    							fields(5),fields(6),fields(7),fields(8),
    				    							fields(9),fields(10).toInt,fields(11).toFloat))
                                                  //> bookings  : List[com.bigdatawave.bookingsanalysis.BookingRecord] = List(Book
                                                  //| ingRecord(1,20141204-103000,AF,153,2145,SENIOR,NCE,CDG,20141220,20141231,2,1
                                                  //| 50.0), BookingRecord(2,20141204-103005,AF,124,1462,ADULT,LYS,MRS,20141215,20
                                                  //| 150105,1,115.0), BookingRecord(3,20141204-103005,AF,147,1462,ADULT,LYO,MAR,2
                                                  //| 0141215,20150105,1,130.0))
   // Group the bookings by departureAirport and destination airport then count the occurrences for each combination
    val airportpairsCount = bookings.groupBy(booking => (booking.departureAirport,booking.destinationAirport)).mapValues(_.size)
                                                  //> airportpairsCount  : scala.collection.immutable.Map[(String, String),Int] = 
                                                  //| Map((NCE,CDG) -> 1, (LYO,MAR) -> 1, (LYS,MRS) -> 1)
    
    
    // Sort the resulting map { (airport1,airport2) -> count} by count value
    // TODO : sortBy not supported on standard map
    //val airportpairsCountOrdered = airportpairsCount.sortBy(_._2 , ascending=false, numPartitions=4)
     				    							
    				    							
	//val citypairsCount = bookings.groupBy(booking => (booking.departureCity,booking.destinationCity)).mapValues(_.size)
  // citypairsCount.toList.sortWith((e1,e2) => (e1._2 > e2._2))
  
  //val citypairsCountOrdered = (citypairsCount.map{ case ((city1,city2),count) => (count,(city1,city2)) }).sortByKey()
  
    				    							
}