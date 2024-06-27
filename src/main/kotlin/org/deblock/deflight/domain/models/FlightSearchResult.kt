package org.deblock.deflight.domain.models

import org.deblock.deflight.infra.inbound.models.FlightSearchResponse
import java.time.LocalDateTime

private const val TWO_DECIMALS_FORMAT = "%.2f"

data class FlightSearchResult(
    val airline: String,
    val supplier: FlightSearchSupplier,
    val cabinClass: CabinClass? = null,
    val fare: Float,
    val origin: AirportCode,
    val destination: AirportCode,
    val departureDate: LocalDateTime,
    val returnDate: LocalDateTime?,
) {
    fun toFlightSearchResponse(): FlightSearchResponse =
        FlightSearchResponse(
            airline = this.airline,
            supplier = this.supplier,
            fare = TWO_DECIMALS_FORMAT.format(this.fare),
            origin = this.origin,
            destination = this.destination,
            departureDate = this.departureDate,
            returnDate = this.returnDate,
        )
}
