package org.deblock.deflight.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["org.deblock.deflight"])
class DeflightApplication

fun main(args: Array<String>) {
    runApplication<DeflightApplication>(*args)
}
