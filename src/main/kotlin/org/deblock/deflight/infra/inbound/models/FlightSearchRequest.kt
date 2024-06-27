package org.deblock.deflight.infra.inbound.models

import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Positive
import org.deblock.deflight.domain.models.AirportCode
import org.deblock.deflight.domain.models.FlightSearch
import java.time.LocalDate

data class FlightSearchRequest(
    val origin: AirportCode,
    val destination: AirportCode,
    @get:FutureOrPresent
    val departureDate: LocalDate,
    @get:FutureOrPresent
    val returnDate: LocalDate? = null,
    @get:Positive @get:Max(4)
    val numberOfPassengers: Int = 1,
) {
    fun toFlightSearch(): FlightSearch =
        FlightSearch(
            origin = this.origin,
            destination = this.destination,
            departureDate = this.departureDate,
            returnDate = this.returnDate,
            numberOfPassengers = this.numberOfPassengers,
        )
}
