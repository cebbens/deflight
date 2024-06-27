package org.deblock.deflight.infra.outbound.config

import feign.Feign
import feign.Logger
import feign.slf4j.Slf4jLogger
import org.deblock.deflight.infra.outbound.crazyair.CrazyAirApi
import org.deblock.deflight.infra.outbound.crazyair.CrazyAirCabinClass
import org.deblock.deflight.infra.outbound.crazyair.CrazyAirFlightSearchRequest
import org.deblock.deflight.infra.outbound.crazyair.CrazyAirFlightSearchResponse
import org.deblock.deflight.infra.outbound.toughjet.ToughJetApi
import org.deblock.deflight.infra.outbound.toughjet.ToughJetFlightSearchRequest
import org.deblock.deflight.infra.outbound.toughjet.ToughJetFlightSearchResponse
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.cloud.openfeign.support.SpringDecoder
import org.springframework.cloud.openfeign.support.SpringEncoder
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.ZoneId

@Configuration
class SupplierConfig {

    // Mocked supplier's API clients that return hard-coded data for testing purposes
    @Bean
    @Profile("dev")
    fun mockedCrazyAirApi(): CrazyAirApi =
        object : CrazyAirApi {
            override suspend fun search(crazyAirFlightSearchRequest: CrazyAirFlightSearchRequest): List<CrazyAirFlightSearchResponse> =
                listOf(
                    CrazyAirFlightSearchResponse(
                        airline = "Foo Airlines",
                        price = 99.99F,
                        cabinClass = CrazyAirCabinClass.E,
                        departureAirportCode = crazyAirFlightSearchRequest.origin,
                        destinationAirportCode = crazyAirFlightSearchRequest.destination,
                        departureDate = crazyAirFlightSearchRequest.departureDate.atStartOfDay(),
                        arrivalDate = crazyAirFlightSearchRequest.returnDate?.atStartOfDay()
                    ),
                    CrazyAirFlightSearchResponse(
                        airline = "Bar Airlines",
                        price = 109.99F,
                        cabinClass = CrazyAirCabinClass.E,
                        departureAirportCode = crazyAirFlightSearchRequest.origin,
                        destinationAirportCode = crazyAirFlightSearchRequest.destination,
                        departureDate = crazyAirFlightSearchRequest.departureDate.atStartOfDay(),
                        arrivalDate = crazyAirFlightSearchRequest.returnDate?.atStartOfDay()
                    ),
                )
        }

    @Bean
    @Profile("dev")
    fun mockedToughJetApi(): ToughJetApi =
        object : ToughJetApi {
            override suspend fun search(toughJetFlightSearchRequest: ToughJetFlightSearchRequest): List<ToughJetFlightSearchResponse> =
                listOf(
                    ToughJetFlightSearchResponse(
                        carrier = "Foo Airlines",
                        basePrice = 100F,
                        tax = 10F,
                        discount = 10F,
                        departureAirportName = toughJetFlightSearchRequest.from,
                        arrivalAirportName = toughJetFlightSearchRequest.to,
                        outboundDateTime = toughJetFlightSearchRequest.outboundDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant(),
                        inboundDateTime = toughJetFlightSearchRequest.inboundDate?.atStartOfDay()?.atZone(ZoneId.systemDefault())?.toInstant(),
                    ),
                    ToughJetFlightSearchResponse(
                        carrier = "Bar Airlines",
                        basePrice = 95F,
                        tax = 15F,
                        discount = 10F,
                        departureAirportName = toughJetFlightSearchRequest.from,
                        arrivalAirportName = toughJetFlightSearchRequest.to,
                        outboundDateTime = toughJetFlightSearchRequest.outboundDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant(),
                        inboundDateTime = toughJetFlightSearchRequest.inboundDate?.atStartOfDay()?.atZone(ZoneId.systemDefault())?.toInstant(),
                    ),
                )
        }

    // Actual Feign supplier's API clients that would be used in a production environment
    @Bean
    @Profile("prod")
    fun crazyAirApi(@Value("\${crazyair.api.endpoint}") endpoint: String): CrazyAirApi =
        Feign.builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .contract(SpringMvcContract())
            .encoder(springEncoder())
            .decoder(springDecoder())
            .target(CrazyAirApi::class.java, endpoint)

    @Bean
    @Profile("prod")
    fun toughJetApi(@Value("\${toughjet.api.endpoint}") endpoint: String): ToughJetApi =
        Feign.builder()
            .logger(Slf4jLogger())
            .logLevel(Logger.Level.BASIC)
            .contract(SpringMvcContract())
            .encoder(springEncoder())
            .decoder(springDecoder())
            .target(ToughJetApi::class.java, endpoint)

    @Bean
    @Profile("prod")
    fun httpMessageConvertersObjectFactory(): ObjectFactory<HttpMessageConverters> = ObjectFactory {
        HttpMessageConverters()
    }

    @Bean
    @Profile("prod")
    fun springEncoder() = SpringEncoder(httpMessageConvertersObjectFactory())

    @Bean
    @Profile("prod")
    fun springDecoder() = SpringDecoder(httpMessageConvertersObjectFactory())
}
