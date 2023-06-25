package tech.aaregall.lab.functions.weather.service.openmeteo

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.service.annotation.GetExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.function.Predicate.not

@ConfigurationProperties("app.open-meteo")
data class OpenMeteoProperties (
    val baseUrl: String,
    val timeout: Duration,
    val hourlyParams: List<String>
)

@Component
class OpenMeteoClient(private val openMeteoProperties: OpenMeteoProperties, webClientBuilder: WebClient.Builder) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val openMeteoHttpClient: OpenMeteoHttpClient = HttpServiceProxyFactory
        .builder(
            WebClientAdapter.forClient(
                webClientBuilder
                    .baseUrl(openMeteoProperties.baseUrl)
                    .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(openMeteoProperties.timeout)))
                    .defaultStatusHandler(not(HttpStatusCode::is2xxSuccessful)) {
                        Mono.error(OpenMeteoException(it.statusCode()))
                    }
                    .build()

            )
        )
        .build()
        .createClient()

    fun getForecast(latitude: Float, longitude: Float): Flux<OpenMeteoForecastResponse> =
        openMeteoHttpClient.getForecast(latitude, longitude, openMeteoProperties.hourlyParams.joinToString(","))
            .doOnError { logger.error("An error occurred while calling OpenMeteo for forecast", it) }

}

private class OpenMeteoException(
    status: HttpStatusCode,
    reason: String? = "Error: OpenMeteo API responded with $status") : ResponseStatusException(status, reason)

private fun interface OpenMeteoHttpClient {

    @GetExchange("/forecast")
    fun getForecast(
        @RequestParam("latitude") latitude: Float,
        @RequestParam("longitude") longitude: Float,
        @RequestParam("hourly") hourly: String
    ): Flux<OpenMeteoForecastResponse>

}