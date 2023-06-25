package tech.aaregall.lab.functions.weather.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import tech.aaregall.lab.functions.weather.domain.Forecast
import tech.aaregall.lab.functions.weather.domain.GeoLocation
import tech.aaregall.lab.functions.weather.domain.HourlyForecast
import tech.aaregall.lab.functions.weather.service.openmeteo.OpenMeteoClient
import tech.aaregall.lab.functions.weather.service.openmeteo.OpenMeteoForecastResponse

@Service
class WeatherService(val openMeteoClient: OpenMeteoClient) {

    fun getForecast(geoLocations: Flux<GeoLocation>): Flux<Forecast> =
        geoLocations.map { openMeteoClient.getForecast(it.latitude, it.longitude) }
            .flatMap { it.map(responseToForecast()) }

    private fun responseToForecast(): (OpenMeteoForecastResponse) -> (Forecast) = {
        Forecast(GeoLocation(it.latitude, it.longitude),
            it.hourly.time.mapIndexed {
                  timeIndex, localDateTime -> HourlyForecast(localDateTime, it.hourly.temperature[timeIndex], it.hourly.precipitation[timeIndex])
            }.toList()
        )
    }

}