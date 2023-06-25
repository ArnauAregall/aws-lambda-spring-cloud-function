package tech.aaregall.lab.functions.weather

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMethod.GET
import org.springframework.http.HttpStatus.OK
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(OutputCaptureExtension::class)
@SpringBootTest
class WeatherFunctionsTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val mockServerClient: MockServerClient) {

    @Nested
    inner class Forecast {

        @ParameterizedTest
        @ValueSource(ints = [400, 404, 503, 504])
        fun `When OpenMeteo API returns client or server errors then returns server error`(statusCode: Int, capturedOutput: CapturedOutput) {
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
                .expectStatus().is2xxSuccessful // limitation on Spring Cloud Function Webflux not being able to control response header
                .expectBody()
                .jsonPath("$").isArray
                .jsonPath("$").isEmpty

            Assertions.assertThat(capturedOutput)
                .contains("An error occurred while calling OpenMeteo for forecast")
                .contains(statusCode.toString())
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
                .jsonPath("$").isArray
                .jsonPath("$").isNotEmpty
                .jsonPath("$[0].geoLocation").isNotEmpty
                .jsonPath("$[0].geoLocation.latitude").isEqualTo(latitude)
                .jsonPath("$[0].geoLocation.longitude").isEqualTo(longitude)
                .jsonPath("$[0].hourlyForecasts").isArray
                .jsonPath("$[0].hourlyForecasts.length()").isEqualTo(2)
                .jsonPath("$[0].hourlyForecasts[0].time").isEqualTo("2023-06-10T00:00:00")
                .jsonPath("$[0].hourlyForecasts[0].temperature").isEqualTo(17.6)
                .jsonPath("$[0].hourlyForecasts[0].precipitation").isEqualTo(1.0)
                .jsonPath("$[0].hourlyForecasts[1].time").isEqualTo("2023-06-10T01:00:00")
                .jsonPath("$[0].hourlyForecasts[1].temperature").isEqualTo(17.2)
                .jsonPath("$[0].hourlyForecasts[1].precipitation").isEqualTo(0.5)
        }

    }


}