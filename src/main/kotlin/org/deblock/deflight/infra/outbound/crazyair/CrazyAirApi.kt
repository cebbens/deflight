package org.deblock.deflight.infra.outbound.crazyair

import org.springframework.web.bind.annotation.PostMapping

/**
 * Interface thar represents CrazyAir API entry point and is used for the Feign client creation
 */
interface CrazyAirApi {

    @PostMapping("/search")
    suspend fun search(crazyAirFlightSearchRequest: CrazyAirFlightSearchRequest): List<CrazyAirFlightSearchResponse>
}
