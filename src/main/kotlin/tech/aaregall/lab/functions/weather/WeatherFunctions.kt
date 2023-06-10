package tech.aaregall.lab.functions.weather

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import reactor.core.publisher.Mono
import tech.aaregall.lab.functions.weather.domain.Forecast
import tech.aaregall.lab.functions.weather.domain.GeoLocation
import tech.aaregall.lab.functions.weather.service.WeatherService

@Configuration
class WeatherFunctions {

    @Bean
    @RegisterReflectionForBinding(classes = [GeoLocation::class, Forecast::class])
    fun forecast(weatherService: WeatherService): (Message<GeoLocation>) -> Mono<Forecast> = weatherService::getForecast

}