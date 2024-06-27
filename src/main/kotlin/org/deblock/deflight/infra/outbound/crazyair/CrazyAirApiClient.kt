package org.deblock.deflight.infra.outbound.crazyair

import org.deblock.deflight.domain.models.FlightSearch
import org.deblock.deflight.domain.models.FlightSearchResult
import org.deblock.deflight.domain.ports.FlightSearchServicePort
import org.springframework.stereotype.Component

@Component
class CrazyAirApiClient(private val crazyAirApi: CrazyAirApi): FlightSearchServicePort {
    override suspend fun search(flightSearch: FlightSearch): List<FlightSearchResult> {
        val request = flightSearch.toCrazyAirFlightSearchRequest()

        val results = crazyAirApi.search(request)

        return results.map { it.toFlightSearchResult() }
    }
}

fun FlightSearch.toCrazyAirFlightSearchRequest(): CrazyAirFlightSearchRequest =
    CrazyAirFlightSearchRequest(
        origin = this.origin,
        destination = this.destination,
        departureDate = this.departureDate,
        returnDate = this.returnDate,
        passengerCount = this.numberOfPassengers,
    )
