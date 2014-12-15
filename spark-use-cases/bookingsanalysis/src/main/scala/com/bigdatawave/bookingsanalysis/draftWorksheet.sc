package com.bigdatawave.bookingsanalysis
import scala.io.Source
object draftWorksheet {
	val file = Source.fromFile("/home/edy/Work/Projects/Spark/sparkrepoeric/spark-use-cases/data/booking-data/demo-bookings.csv")
                                                  //> file  : scala.io.BufferedSource = non-empty iterator
  file.getLines().foreach ( line =>
            {
              Thread.sleep(1000)
              // write the byte to the socket
         
              // also print the byte to stdout, for debugging ease
              println(line)
            }
           )                                      //> 1,20141204-103000,AF,153,2145,SENIOR,NCE,CDG,20141220,20141231,2,150
                                                  //| 2,20141204-103005,AF,124,1462,ADULT,LYS,MRS,20141215,20150105,1,115
                                                  //| 3,20141206-154500,BA,326,4527,ADULT,LGW,BOD,20150107,20150121,1,200
                                                  //| 4,20141207-112000,AL,147,5982,INFANT,FCO,TRN,20150204,20150210,1,100
                                                  //| 5,20141207-155010,LF,289,9740,ADULT,NCE,CDG,20150208,20150220,2,310
                                                  //| 6,20141207-171200,SW,350,1598,ADULT,LYS,MRS,20150208,20150215,1,125
                                                  //| 7,20141208-112810,LF,314,3985,SENIOR,NCE,ORY,20150212,20150218,2,345
                                                  //| 8,20141210-201300,BR,894,3785,ADULT,BRU,SXB,20150220,20150228,1,150
}