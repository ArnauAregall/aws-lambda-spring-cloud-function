package tech.aaregall.lab.functions.weather

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import tech.aaregall.lab.functions.question.domain.Question
import tech.aaregall.lab.functions.weather.domain.Forecast
import tech.aaregall.lab.functions.weather.domain.GeoLocation
import tech.aaregall.lab.functions.weather.service.WeatherService

@Configuration
class WeatherFunctions {

    @Bean
    fun forecast(weatherService: WeatherService): (Flux<GeoLocation>) -> Flux<Forecast> = weatherService::getForecast

    @Bean
    fun forecastToQuestion(): (Flux<Forecast>) -> Flux<Question> = {
        it.map { forecast -> Question(listOf(
            "Given the following weather forecast on coordinates ${forecast.geoLocation.latitude}, ${forecast.geoLocation.longitude}:",
            forecast.hourlyForecasts.joinToString("\n"),
            "Write a 1 to 3 sentence summary of the weather forecast in common vocabulary.",
            "Include the start and end dates of the period in the following format: Monday 1st of January",
            "Use the character º to indicate the temperature is in Celsius.",
            "Include highest and lowest temperature indications of the whole period.",
            "Include the chance of rain.",
            "Include the location name in the following format: City (Country).",
        )) }
    }

}