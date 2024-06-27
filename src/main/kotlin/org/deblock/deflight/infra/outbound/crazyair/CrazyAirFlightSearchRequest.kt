package org.deblock.deflight.infra.outbound.crazyair

import org.deblock.deflight.domain.models.AirportCode
import java.time.LocalDate

data class CrazyAirFlightSearchRequest(
    val origin: AirportCode,
    val destination: AirportCode,
    val departureDate: LocalDate,
    val returnDate: LocalDate? = null,
    val passengerCount: Int = 1,
)
