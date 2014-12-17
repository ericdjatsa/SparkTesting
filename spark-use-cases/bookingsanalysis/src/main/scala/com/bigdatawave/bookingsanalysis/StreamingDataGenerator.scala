package com.bigdatawave.bookingsanalysis

import java.io.PrintWriter
import java.net.ServerSocket
import scala.io.Source

// [source : http://apache-spark-user-list.1001560.n3.nabble.com/streaming-code-to-simulate-a-network-socket-data-source-tc3431.html ]
//simple script that simply reads a file off disk and passes it to a socket, character by character.  
//You specify the port, filename, and bytesPerSecond that you want it to send.
object StreamingDataGenerator {
	def main(args : Array[String]) {
    if (args.length != 3) {
      System.err.println("Usage: StreamingDataGenerator <port> <inpufile> <linesPerSecond>")
      System.exit(1)
    }
    val port = args(0).toInt
    val file = Source.fromFile(args(1))
    val linesPerSecond = args(2).toFloat
    
    val sleepDelayMs = (1000.0 / linesPerSecond).toInt
    val listener = new ServerSocket(port)
    
    println("Reading from file: " + args(1))

    while (true) {
      println("Listening on port: " + port)
      val socket = listener.accept()
      new Thread() {
        override def run = {
          println("Got client connect from: " + socket.getInetAddress)
          val out = new PrintWriter(socket.getOutputStream(), true)

           file.getLines().foreach ( line => 
            {
              Thread.sleep(sleepDelayMs)
              // write the line to the socket
              out.println(line)
              out.flush()
              // also print the line to stdout, for debugging ease
              println(line)
            }
          )
          socket.close()
        }
      }.start()
    }
  }
}