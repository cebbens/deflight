package org.deblock.deflight.domain.services

import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.deblock.deflight.app.services.FlightSearchService
import org.deblock.deflight.domain.exceptions.InvalidDatePeriodException
import org.deblock.deflight.domain.models.AirportCode
import org.deblock.deflight.domain.models.CabinClass
import org.deblock.deflight.domain.models.FlightSearch
import org.deblock.deflight.domain.models.FlightSearchResult
import org.deblock.deflight.domain.models.FlightSearchSupplier
import org.deblock.deflight.infra.outbound.crazyair.CrazyAirApiClient
import org.deblock.deflight.infra.outbound.toughjet.ToughJetApiClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.internal.verification.VerificationModeFactory.noInteractions
import org.mockito.junit.jupiter.MockitoExtension
import java.time.LocalDate
import kotlin.system.measureTimeMillis

@ExtendWith(MockitoExtension::class)
class FlightSearchServiceTest {

    private lateinit var flightSearchService: FlightSearchService

    @Mock
    private lateinit var crazyAirApiClient: CrazyAirApiClient

    @Mock
    private lateinit var toughJetApiClient: ToughJetApiClient

    @BeforeEach
    fun setup() {
        flightSearchService = FlightSearchService(listOf(crazyAirApiClient, toughJetApiClient))
    }

    @Test
    fun `should successfully search flights`() {
        val departureDate = LocalDate.now()
        val returnDate = departureDate.plusDays(3)
        val flightSearch = FlightSearch(
            origin = AirportCode.AMS,
            destination = AirportCode.LHR,
            departureDate = departureDate,
            returnDate = returnDate,
        )

        val expectedCrazyAirFlights = listOf(
            FlightSearchResult(
                airline = "Foo Airlines",
                supplier = FlightSearchSupplier.CRAZY_AIR,
                fare = 99.99F,
                cabinClass = CabinClass.ECONOMY,
                origin = flightSearch.origin,
                destination = flightSearch.destination,
                departureDate = flightSearch.departureDate.atStartOfDay(),
                returnDate = flightSearch.returnDate?.atStartOfDay(),
            ),
            FlightSearchResult(
                airline = "Bar Airlines",
                supplier = FlightSearchSupplier.CRAZY_AIR,
                fare = 109.99F,
                cabinClass = CabinClass.ECONOMY,
                origin = flightSearch.origin,
                destination = flightSearch.destination,
                departureDate = flightSearch.departureDate.atStartOfDay(),
                returnDate = flightSearch.returnDate?.atStartOfDay(),
            ),
        )

        val expectedToughJetFlights = listOf(
            FlightSearchResult(
                airline = "Foo Airlines",
                supplier = FlightSearchSupplier.TOUGH_JET,
                fare = 100F,
                origin = flightSearch.origin,
                destination = flightSearch.destination,
                departureDate = flightSearch.departureDate.atStartOfDay(),
                returnDate = flightSearch.returnDate?.atStartOfDay(),
            ),
            FlightSearchResult(
                airline = "Bar Airlines",
                supplier = FlightSearchSupplier.TOUGH_JET,
                fare = 95F,
                origin = flightSearch.origin,
                destination = flightSearch.destination,
                departureDate = flightSearch.departureDate.atStartOfDay(),
                returnDate = flightSearch.returnDate?.atStartOfDay(),
            ),
        )
        runBlocking {
            `when`(crazyAirApiClient.search(flightSearch)).thenReturn(expectedCrazyAirFlights)
            `when`(toughJetApiClient.search(flightSearch)).thenReturn(expectedToughJetFlights)

            val results = flightSearchService.search(flightSearch)

            assertThat(results).hasSize(4)
        }
    }

    @Test
    fun `should parallelize searching flights through all the suppliers`() {
        val departureDate = LocalDate.now()
        val returnDate = departureDate.plusDays(3)
        val flightSearch = FlightSearch(
            origin = AirportCode.LHR,
            destination = AirportCode.AMS,
            departureDate = departureDate,
            returnDate = returnDate,
        )
        val timeMillis = 1000L
        runBlocking {
            `when`(crazyAirApiClient.search(flightSearch)).thenReturn(
                async { emptyList<FlightSearchResult>().also { delay(timeMillis) } }.await()
            )
            `when`(toughJetApiClient.search(flightSearch)).thenReturn(
                async { emptyList<FlightSearchResult>().also { delay(timeMillis) } }.await()
            )

            val executionTimeMillis = measureTimeMillis {
                flightSearchService.search(flightSearch)
            }

            assertThat(executionTimeMillis).isLessThan(timeMillis * 2)
            verify(crazyAirApiClient).search(flightSearch)
            verify(toughJetApiClient).search(flightSearch)
        }
    }

    @Test
    fun `should throw InvalidDatePeriodException when searching flights with return date before departure date`() {
        val departureDate = LocalDate.now()
        val returnDate = departureDate.minusDays(3)
        val flightSearch = FlightSearch(
            origin = AirportCode.LHR,
            destination = AirportCode.AMS,
            departureDate = departureDate,
            returnDate = returnDate,
        )

        runBlocking {
            assertThrows<InvalidDatePeriodException> {
                flightSearchService.search(flightSearch)
            }

            verify(crazyAirApiClient, noInteractions()).search(flightSearch)
            verify(toughJetApiClient, noInteractions()).search(flightSearch)
        }
    }
}
