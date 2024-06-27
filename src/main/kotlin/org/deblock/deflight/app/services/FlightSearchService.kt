package org.deblock.deflight.app.services

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.deblock.deflight.domain.exceptions.InvalidDatePeriodException
import org.deblock.deflight.domain.models.FlightSearch
import org.deblock.deflight.domain.models.FlightSearchResult
import org.deblock.deflight.domain.ports.FlightSearchServicePort
import org.springframework.stereotype.Service

@Service
class FlightSearchService(private val flightSearchSuppliers: List<FlightSearchServicePort>) : FlightSearchServicePort {

    /**
     * Main service which parallelize supplier request and aggregates the responses
     */
    override suspend fun search(flightSearch: FlightSearch): List<FlightSearchResult> {
        if (flightSearch.departureDate.isAfter(flightSearch.returnDate)) {
            throw InvalidDatePeriodException()
        }

        return coroutineScope {
            flightSearchSuppliers
                .map { async { it.search(flightSearch) } }
                .awaitAll()
                .flatten()
        }
    }
}
