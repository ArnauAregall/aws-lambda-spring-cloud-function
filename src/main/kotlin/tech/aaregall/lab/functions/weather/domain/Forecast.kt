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
) {

    override fun toString(): String {
        return "At $time, temperature will be $temperature º, precipitation of $precipitation mm"
    }

}