package org.deblock.deflight.infra.inbound.models

import java.time.Instant

data class ErrorResponse(
    val timestamp: Instant = Instant.now(),
    val errors: List<Error>,
) {
    data class Error(
        val source: String? = null,
        val message: String? = null
    )
}
