package org.deblock.deflight

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DeflightApplication

fun main(args: Array<String>) {
    runApplication<DeflightApplication>(*args)
}
