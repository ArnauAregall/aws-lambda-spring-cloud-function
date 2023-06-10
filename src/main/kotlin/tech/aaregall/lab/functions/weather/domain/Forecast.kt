package tech.aaregall.lab.functions.weather.domain

import java.time.LocalDateTime

data class Forecast(
    val geoLocation: GeoLocation,
    val hourlyForecasts: List<HourlyForecast>
)

data class HourlyForecast (
    val time: LocalDateTime,
    val temperature: Float,
    val precipitation: Float
)