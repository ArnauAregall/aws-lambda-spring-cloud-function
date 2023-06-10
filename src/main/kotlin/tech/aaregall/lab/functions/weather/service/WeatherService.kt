package tech.aaregall.lab.functions.weather.service

import org.springframework.messaging.Message
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import tech.aaregall.lab.functions.weather.domain.Forecast
import tech.aaregall.lab.functions.weather.domain.GeoLocation
import tech.aaregall.lab.functions.weather.domain.HourlyForecast
import tech.aaregall.lab.functions.weather.service.openmeteo.OpenMeteoClient
import tech.aaregall.lab.functions.weather.service.openmeteo.OpenMeteoForecastResponse

@Service
class WeatherService(val openMeteoClient: OpenMeteoClient) {

    fun getForecast(message: Message<GeoLocation>): Mono<Forecast> {
        return openMeteoClient.getForecast(message.payload.latitude, message.payload.longitude)
            .map(responseToForecast())
    }

    private fun responseToForecast(): (OpenMeteoForecastResponse) -> (Forecast) = {
        Forecast(GeoLocation(it.latitude, it.longitude),
            it.hourly.time.mapIndexed {
                  timeIndex, localDateTime -> HourlyForecast(localDateTime, it.hourly.temperature[timeIndex], it.hourly.precipitation[timeIndex])
            }.toList()
        )
    }

}