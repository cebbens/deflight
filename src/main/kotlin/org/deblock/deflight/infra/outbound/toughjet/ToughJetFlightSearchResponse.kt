package org.deblock.deflight.infra.outbound.toughjet

import org.deblock.deflight.domain.models.AirportCode
import org.deblock.deflight.domain.models.FlightSearchResult
import org.deblock.deflight.domain.models.FlightSearchSupplier
import java.time.Instant
import java.time.ZoneId

data class ToughJetFlightSearchResponse(
    val carrier: String,
    val basePrice: Float,
    val tax: Float,
    val discount: Float,
    val departureAirportName: AirportCode,
    val arrivalAirportName: AirportCode,
    val outboundDateTime: Instant,
    val inboundDateTime: Instant?,
) {
    fun toFlightSearchResult(): FlightSearchResult =
        FlightSearchResult(
            airline = this.carrier,
            supplier = FlightSearchSupplier.TOUGH_JET,
            fare = calculateFare(),
            origin = this.departureAirportName,
            destination = this.arrivalAirportName,
            departureDate = this.outboundDateTime.atZone(ZoneId.systemDefault()).toLocalDateTime(),
            returnDate = this.inboundDateTime?.atZone(ZoneId.systemDefault())?.toLocalDateTime(),
        )

    private fun calculateFare() = this.basePrice * (1 - this.discount / 100) + this.tax
}
