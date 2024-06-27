package org.deblock.deflight.infra.inbound

import jakarta.validation.Valid
import org.deblock.deflight.app.services.FlightSearchService
import org.deblock.deflight.infra.inbound.models.DataResponse
import org.deblock.deflight.infra.inbound.models.FlightSearchRequest
import org.deblock.deflight.infra.inbound.models.FlightSearchResponse
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Validated
@RestController
@RequestMapping("/flights", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
class FlightSearchController(private val flightSearchService: FlightSearchService) {

    @PostMapping("/search")
    suspend fun search(@Valid @RequestBody flightSearchRequest: FlightSearchRequest): DataResponse<List<FlightSearchResponse>> {
        val flightSearch = flightSearchRequest.toFlightSearch()

        val results = flightSearchService.search(flightSearch)

        return DataResponse(data = results.sortedBy { it.fare }.map { it.toFlightSearchResponse() })
    }
}
