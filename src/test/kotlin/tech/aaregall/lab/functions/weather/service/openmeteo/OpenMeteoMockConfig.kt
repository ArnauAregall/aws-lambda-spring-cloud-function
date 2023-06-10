package tech.aaregall.lab.functions.weather.service.openmeteo

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.testcontainers.containers.MockServerContainer
import java.time.Duration

@Configuration
class OpenMeteoMockConfig {

    @Bean
    @Primary
    @ConfigurationProperties
    fun openMeteoProperties(mockServerContainer: MockServerContainer): OpenMeteoProperties =
        OpenMeteoProperties(mockServerContainer.endpoint, Duration.ofSeconds(1), listOf("temperature", "precipitation"))

}