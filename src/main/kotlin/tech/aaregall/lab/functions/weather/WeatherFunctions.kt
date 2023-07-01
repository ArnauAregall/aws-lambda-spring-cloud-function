package tech.aaregall.lab.functions.weather

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import tech.aaregall.lab.functions.weather.domain.Forecast
import tech.aaregall.lab.functions.weather.domain.GeoLocation
import tech.aaregall.lab.functions.weather.service.WeatherService

@Configuration
class WeatherFunctions {

    @Bean
    fun forecast(weatherService: WeatherService): (Flux<GeoLocation>) -> Flux<Forecast> = weatherService::getForecast

}