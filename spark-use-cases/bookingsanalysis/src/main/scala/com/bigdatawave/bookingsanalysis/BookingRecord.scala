package com.bigdatawave.bookingsanalysis

// Case class BookingRecord for representing a Booking
case class BookingRecord (bookingID: String, 
						 bookingTimestamp: String, 
						 carrierCode: String, 
						 flightID: String,
						 customerID:String,
						 ageCategory:String,
						 departureAirport : String,
						 destinationAirport : String,
						 departureDate : String,
						 returnDate : String,
						 nbPassengers : Int,
						 ticketPrice : Float)