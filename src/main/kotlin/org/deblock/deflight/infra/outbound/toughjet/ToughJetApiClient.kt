package org.deblock.deflight.infra.outbound.toughjet

import org.deblock.deflight.domain.models.FlightSearch
import org.deblock.deflight.domain.models.FlightSearchResult
import org.deblock.deflight.domain.ports.FlightSearchServicePort
import org.springframework.stereotype.Component

@Component
class ToughJetApiClient(private val toughJetApi: ToughJetApi): FlightSearchServicePort {
    override suspend fun search(flightSearch: FlightSearch): List<FlightSearchResult> {
        val request = flightSearch.toToughJetFlightSearchRequest()

        val results = toughJetApi.search(request)

        return results.map { it.toFlightSearchResult() }
    }
}

fun FlightSearch.toToughJetFlightSearchRequest(): ToughJetFlightSearchRequest =
    ToughJetFlightSearchRequest(
        from = this.origin,
        to = this.destination,
        outboundDate = this.departureDate,
        inboundDate = this.returnDate,
        numberOfAdults = this.numberOfPassengers,
    )
