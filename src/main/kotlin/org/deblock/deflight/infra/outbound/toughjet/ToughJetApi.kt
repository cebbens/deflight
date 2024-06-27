package org.deblock.deflight.infra.outbound.toughjet

import org.springframework.web.bind.annotation.PostMapping

/**
 * Interface thar represents ToughJet API entry point and is used for the Feign client creation
 */
interface ToughJetApi {

    @PostMapping("/search")
    suspend fun search(toughJetFlightSearchRequest: ToughJetFlightSearchRequest): List<ToughJetFlightSearchResponse>
}
