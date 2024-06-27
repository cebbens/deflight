package org.deblock.deflight.infra.inbound.models

import org.deblock.deflight.domain.models.AirportCode
import org.deblock.deflight.domain.models.FlightSearchSupplier
import java.time.LocalDateTime

data class FlightSearchResponse(
    val airline: String,
    val supplier: FlightSearchSupplier,
    val fare: String,
    val origin: AirportCode,
    val destination: AirportCode,
    val departureDate: LocalDateTime,
    val returnDate: LocalDateTime?,
)
