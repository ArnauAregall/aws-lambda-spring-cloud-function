package tech.aaregall.lab.functions.weather

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import reactor.core.publisher.Mono
import tech.aaregall.lab.functions.question.domain.Question
import tech.aaregall.lab.functions.weather.domain.Forecast
import tech.aaregall.lab.functions.weather.domain.GeoLocation
import tech.aaregall.lab.functions.weather.service.WeatherService

@Configuration
@RegisterReflectionForBinding(classes = [GeoLocation::class, Forecast::class])
class WeatherFunctions {

    @Bean
    fun forecast(weatherService: WeatherService): (Message<GeoLocation>) -> Mono<Forecast> = weatherService::getForecast

    @Bean
    fun forecastToQuestion(): (Message<Forecast>) -> Question = {
        Question(listOf(
            "Given the following weather forecast:",
            it.payload.hourlyForecasts.joinToString("\n"),
            "Write a 1 to 3 sentence summary of the weather forecast in common vocabulary.",
            "Specify the start and end period in a human readable date format.",
            "Use the character º to indicate the temperature is in Celsius.",
            "Include highest and lowest temperature indications of the whole period.",
            "Include the chance of rain."
        ))
    }

}