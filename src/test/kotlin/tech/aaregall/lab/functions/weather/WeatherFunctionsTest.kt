package tech.aaregall.lab.functions.weather

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
class WeatherFunctionsTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val mockServerClient: MockServerClient) {

    @Nested
    inner class Forecast {

        @ParameterizedTest
        @EmptySource
        fun `When body is empty then returns server error`(body: String) {
            webTestClient.post().uri("/forecast")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .bodyValue(body)
                .exchange()
                .expectHeader().contentType(APPLICATION_JSON_VALUE)
                .expectStatus().is5xxServerError
        }

        @ParameterizedTest
        @ValueSource(ints = [400, 404, 503, 504])
        fun `When OpenMeteo API returns client or server errors then returns server error`(statusCode: Int) {
            mockServerClient.reset()
                .`when`(request()
                .withMethod(GET.name())
                .withPath("/forecast")
                .withQueryStringParameter("latitude")
                .withQueryStringParameter("longitude")
            ).respond(response().withStatusCode(statusCode))

            webTestClient.post().uri("/forecast")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .bodyValue("""
                    {"latitude": 1.12, "longitude": 2.23}
                """.trimIndent())
                .exchange()
                .expectHeader().contentType(APPLICATION_JSON_VALUE)
                .expectStatus().is5xxServerError
        }

        @Test
        fun `When OpenMeteo API returns results then returns OK and response contains Forecast`() {
            val latitude = 1.12
            val longitude = 2.23

            mockServerClient.reset()
                .`when`(
                    request()
                        .withMethod(GET.name())
                        .withPath("/forecast")
                        .withQueryStringParameter("latitude", latitude.toString())
                        .withQueryStringParameter("longitude", longitude.toString())
                ).respond(
                    response()
                        .withStatusCode(OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                                "latitude": $latitude,
                                "longitude": $longitude,
                                "hourly": {
                                    "time": ["2023-06-10T00:00", "2023-06-10T01:00"],
                                    "temperature_2m": [17.6, 17.2],
                                    "precipitation": [1.0, 0.5]
                                }
                            }
                        """.trimIndent()))

            webTestClient
                .post()
                .uri("/forecast")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .bodyValue("""
                    {"latitude": $latitude, "longitude": $longitude}
                """.trimIndent())
                .exchange()
                .expectHeader().contentType(APPLICATION_JSON_VALUE)
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$.geoLocation").isNotEmpty
                .jsonPath("$.geoLocation.latitude").isEqualTo(latitude)
                .jsonPath("$.geoLocation.longitude").isEqualTo(longitude)
                .jsonPath("$.hourlyForecasts").isArray
                .jsonPath("$.hourlyForecasts.length()").isEqualTo(2)
                .jsonPath("$.hourlyForecasts[0].time").isEqualTo("2023-06-10T00:00:00")
                .jsonPath("$.hourlyForecasts[0].temperature").isEqualTo(17.6)
                .jsonPath("$.hourlyForecasts[0].precipitation").isEqualTo(1.0)
                .jsonPath("$.hourlyForecasts[1].time").isEqualTo("2023-06-10T01:00:00")
                .jsonPath("$.hourlyForecasts[1].temperature").isEqualTo(17.2)
                .jsonPath("$.hourlyForecasts[1].precipitation").isEqualTo(0.5)
        }

    }


}