package org.deblock.deflight.infra.inbound.models

import java.time.Instant

data class DataResponse<T>(
    val timestamp: Instant = Instant.now(),
    val data: T,
)
