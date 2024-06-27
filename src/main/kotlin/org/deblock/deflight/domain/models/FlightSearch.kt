package org.deblock.deflight.domain.models

import java.time.LocalDate

data class FlightSearch(
    val origin: AirportCode,
    val destination: AirportCode,
    val departureDate: LocalDate,
    val returnDate: LocalDate? = null,
    val numberOfPassengers: Int = 1,
)
