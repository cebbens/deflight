package org.deblock.deflight.infra.outbound.toughjet

import org.deblock.deflight.domain.models.AirportCode
import java.time.LocalDate

data class ToughJetFlightSearchRequest(
    val from: AirportCode,
    val to: AirportCode,
    val outboundDate: LocalDate,
    val inboundDate: LocalDate? = null,
    val numberOfAdults: Int = 1,
)
