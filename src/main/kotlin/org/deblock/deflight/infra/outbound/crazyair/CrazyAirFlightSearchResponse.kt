package org.deblock.deflight.infra.outbound.crazyair

import com.fasterxml.jackson.annotation.JsonProperty
import org.deblock.deflight.domain.models.AirportCode
import org.deblock.deflight.domain.models.CabinClass
import org.deblock.deflight.domain.models.FlightSearchResult
import org.deblock.deflight.domain.models.FlightSearchSupplier
import java.time.LocalDateTime

data class CrazyAirFlightSearchResponse(
    val airline: String,
    val price: Float,
    @JsonProperty("cabinclass")
    val cabinClass: CrazyAirCabinClass,
    val departureAirportCode: AirportCode,
    val destinationAirportCode: AirportCode,
    val departureDate: LocalDateTime,
    val arrivalDate: LocalDateTime?,
) {
    fun toFlightSearchResult(): FlightSearchResult =
        FlightSearchResult(
            airline = this.airline,
            supplier = FlightSearchSupplier.CRAZY_AIR,
            cabinClass = cabinClass.toCabinClass(),
            fare = this.price,
            origin = this.departureAirportCode,
            destination = this.destinationAirportCode,
            departureDate = this.departureDate,
            returnDate = this.arrivalDate,
        )
}

enum class CrazyAirCabinClass {
    E,
    B;

    fun toCabinClass(): CabinClass =
        when(this) {
            E -> CabinClass.ECONOMY
            B -> CabinClass.BUSINESS
        }
}
