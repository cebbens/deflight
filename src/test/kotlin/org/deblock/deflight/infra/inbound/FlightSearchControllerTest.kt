package org.deblock.deflight.infra.inbound

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.deblock.deflight.app.DeflightApplication
import org.deblock.deflight.app.services.FlightSearchService
import org.deblock.deflight.domain.models.AirportCode
import org.deblock.deflight.infra.inbound.models.DataResponse
import org.deblock.deflight.infra.inbound.models.FlightSearchRequest
import org.deblock.deflight.infra.inbound.models.FlightSearchResponse
import org.deblock.deflight.infra.outbound.crazyair.CrazyAirApi
import org.deblock.deflight.infra.outbound.crazyair.CrazyAirCabinClass
import org.deblock.deflight.infra.outbound.crazyair.CrazyAirFlightSearchResponse
import org.deblock.deflight.infra.outbound.crazyair.toCrazyAirFlightSearchRequest
import org.deblock.deflight.infra.outbound.toughjet.ToughJetApi
import org.deblock.deflight.infra.outbound.toughjet.ToughJetFlightSearchResponse
import org.deblock.deflight.infra.outbound.toughjet.toToughJetFlightSearchRequest
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.request
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.time.ZoneId

private const val SEARCH_URI = "/flights/search"

@SpringBootTest(classes = [DeflightApplication::class])
@AutoConfigureMockMvc
class FlightSearchControllerTest {

    @MockBean
    private lateinit var crazyAirApi: CrazyAirApi
    @MockBean
    private lateinit var toughJetApi: ToughJetApi
    @Autowired
    private lateinit var mockMvc: MockMvc

    private var objectMapper: ObjectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @Test
    fun `should successfully search flights and return results sorted by fare`(): Unit = runBlocking {
        val departureDate = LocalDate.now()
        val returnDate = departureDate.plusDays(3)
        val flightSearchRequestJson = """
            {
                "origin": "${AirportCode.LHR}",
                "destination": "${AirportCode.AMS}",
                "departureDate": "$departureDate",
                "returnDate": "$returnDate",
                "numberOfPassengers": 3
            }
        """.trimIndent()
        val flightSearch = objectMapper.readValue<FlightSearchRequest>(flightSearchRequestJson).toFlightSearch()

        val crazyAirFlightSearchRequest = flightSearch.toCrazyAirFlightSearchRequest()
        val expectedCrazyAirFlights = listOf(
            CrazyAirFlightSearchResponse(
                airline = "Foo Airlines",
                price = 99.99F,
                cabinClass = CrazyAirCabinClass.E,
                departureAirportCode = flightSearch.origin,
                destinationAirportCode = flightSearch.destination,
                departureDate = flightSearch.departureDate.atStartOfDay(),
                arrivalDate = flightSearch.returnDate?.atStartOfDay()
            ),
            CrazyAirFlightSearchResponse(
                airline = "Bar Airlines",
                price = 109.99F,
                cabinClass = CrazyAirCabinClass.E,
                departureAirportCode = flightSearch.origin,
                destinationAirportCode = flightSearch.destination,
                departureDate = flightSearch.departureDate.atStartOfDay(),
                arrivalDate = flightSearch.returnDate?.atStartOfDay()
            ),
        )
        `when`(crazyAirApi.search(crazyAirFlightSearchRequest)).thenReturn(expectedCrazyAirFlights)

        val toughJetFlightSearchRequest = flightSearch.toToughJetFlightSearchRequest()
        val expectedToughJetFlights = listOf(
            ToughJetFlightSearchResponse(
                carrier = "Foo Airlines",
                basePrice = 100F,
                tax = 10F,
                discount = 10F,
                departureAirportName = flightSearch.origin,
                arrivalAirportName = flightSearch.destination,
                outboundDateTime = flightSearch.departureDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant(),
                inboundDateTime = flightSearch.returnDate?.atStartOfDay()?.atZone(ZoneId.systemDefault())?.toInstant(),
            ),
            ToughJetFlightSearchResponse(
                carrier = "Bar Airlines",
                basePrice = 95F,
                tax = 15F,
                discount = 10F,
                departureAirportName = flightSearch.origin,
                arrivalAirportName = flightSearch.destination,
                outboundDateTime = flightSearch.departureDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant(),
                inboundDateTime = flightSearch.returnDate?.atStartOfDay()?.atZone(ZoneId.systemDefault())?.toInstant(),
            ),
        )
        `when`(toughJetApi.search(toughJetFlightSearchRequest)).thenReturn(expectedToughJetFlights)

        val expectedFlightSearchResponses =
            (expectedCrazyAirFlights.map { it.toFlightSearchResult() } + expectedToughJetFlights.map { it.toFlightSearchResult() })
            .sortedBy { it.fare }
            .map { it.toFlightSearchResponse() }


        val mvcResult = mockMvc.perform(post(SEARCH_URI).contentType(APPLICATION_JSON).content(flightSearchRequestJson))
            .andExpectAll(
                status().isOk,
                request().asyncStarted()
            ).andReturn()
            .let {
                mockMvc.perform(asyncDispatch(it))
                    .andExpectAll(
                        status().isOk(),
                        content().contentType(APPLICATION_JSON),
                    ).andReturn()
            }


        val dataResponse = objectMapper.readValue<DataResponse<List<FlightSearchResponse>>>(mvcResult.response.contentAsString)
        assertThat(dataResponse.data).hasSize(4)
        assertThat(dataResponse.data).isEqualTo(expectedFlightSearchResponses)
        verify(crazyAirApi).search(crazyAirFlightSearchRequest)
        verify(toughJetApi).search(toughJetFlightSearchRequest)
    }

    @Test
    fun `should fail with bad request on passengers number exceeding maximum`() {
        val flightSearchService: FlightSearchService = mock()
        val departureDate = LocalDate.now()
        val returnDate = departureDate.plusDays(3)
        val flightSearchRequestJson = """
            {
                "origin": "${AirportCode.LHR}",
                "destination": "${AirportCode.AMS}",
                "departureDate": "$departureDate",
                "returnDate": "$returnDate",
                "numberOfPassengers": 7
            }
        """.trimIndent()

        mockMvc.perform(post(SEARCH_URI).contentType(APPLICATION_JSON).content(flightSearchRequestJson))
            .andExpectAll(
                status().isBadRequest,
                content().contentType(APPLICATION_JSON),
                jsonPath("$.errors[0].source").value("numberOfPassengers"),
                jsonPath("$.errors[0].message").value("must be less than or equal to 4"),
            )

        verifyNoInteractions(flightSearchService)
    }

    @Test
    fun `should fail with bad request on passengers number not positive`() {
        val flightSearchService: FlightSearchService = mock()
        val departureDate = LocalDate.now()
        val returnDate = departureDate.plusDays(3)
        val flightSearchRequestJson = """
            {
                "origin": "${AirportCode.LHR}",
                "destination": "${AirportCode.AMS}",
                "departureDate": "$departureDate",
                "returnDate": "$returnDate",
                "numberOfPassengers": 0
            }
        """.trimIndent()

        mockMvc.perform(post(SEARCH_URI).contentType(APPLICATION_JSON).content(flightSearchRequestJson))
            .andExpectAll(
                status().isBadRequest,
                content().contentType(APPLICATION_JSON),
                jsonPath("$.errors[0].source").value("numberOfPassengers"),
                jsonPath("$.errors[0].message").value("must be greater than 0"),
            )

        verifyNoInteractions(flightSearchService)
    }

    @Test
    fun `should fail with bad request on invalid airport code`() {
        val flightSearchService: FlightSearchService = mock()
        val departureDate = LocalDate.now()
        val returnDate = departureDate.plusDays(3)
        val flightSearchRequestJson = """
            {
                "origin": "${AirportCode.LHR}",
                "destination": "EZE",
                "departureDate": "$departureDate",
                "returnDate": "$returnDate",
                "numberOfPassengers": 3
            }
        """.trimIndent()

        mockMvc.perform(post(SEARCH_URI).contentType(APPLICATION_JSON).content(flightSearchRequestJson))
            .andExpectAll(
                status().isBadRequest,
                content().contentType(APPLICATION_JSON),
                jsonPath("$.errors[0].message").value(
                    containsString("JSON parse error: Cannot deserialize value of type `${AirportCode::class.qualifiedName}`")
                ),
            )

        verifyNoInteractions(flightSearchService)
    }

    @Test
    fun `should fail with bad request on past dates`() {
        val flightSearchService: FlightSearchService = mock()
        val departureDate = LocalDate.now().minusDays(3)
        val returnDate = LocalDate.now().minusDays(1)
        val flightSearchRequestJson = """
            {
                "origin": "${AirportCode.LHR}",
                "destination": "${AirportCode.AMS}",
                "departureDate": "$departureDate",
                "returnDate": "$returnDate",
                "numberOfPassengers": 3
            }
        """.trimIndent()

        mockMvc.perform(post(SEARCH_URI).contentType(APPLICATION_JSON).content(flightSearchRequestJson))
            .andExpectAll(
                status().isBadRequest,
                content().contentType(APPLICATION_JSON),
                jsonPath("$.errors", hasSize<Any>(2)),
                jsonPath("$.errors").value(
                    containsInAnyOrder(
                        mapOf("source" to "returnDate", "message" to "must be a date in the present or in the future"),
                        mapOf("source" to "departureDate", "message" to "must be a date in the present or in the future")
                    )
                )
            )

        verifyNoInteractions(flightSearchService)
    }
}
