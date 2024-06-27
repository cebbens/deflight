package org.deblock.deflight.domain.ports

import org.deblock.deflight.domain.models.FlightSearch
import org.deblock.deflight.domain.models.FlightSearchResult

/**
 * Main service port implemented by suppliers and the main service
 */
interface FlightSearchServicePort {

    suspend fun search(flightSearch: FlightSearch): List<FlightSearchResult>
}
