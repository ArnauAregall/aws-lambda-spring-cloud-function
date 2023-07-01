package tech.aaregall.lab.functions.weather.service.openmeteo

import com.fasterxml.jackson.annotation.JsonAlias
import java.time.LocalDateTime

data class OpenMeteoForecastResponse (
    val latitude: Float,
    val longitude: Float,
    val hourly: OpenMeteoHourlyForecast
)

data class OpenMeteoHourlyForecast (
    val time: List<LocalDateTime>,

    @JsonAlias("temperature_2m")
    val temperature: List<Float>,

    val precipitation: List<Float>
)