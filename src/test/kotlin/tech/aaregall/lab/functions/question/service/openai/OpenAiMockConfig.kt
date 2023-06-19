package tech.aaregall.lab.functions.question.service.openai

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.testcontainers.containers.MockServerContainer
import java.time.Duration

@Configuration
class OpenAiMockConfig {

    @Bean
    @Primary
    @ConfigurationProperties
    fun openAiProperties(mockServerContainer: MockServerContainer): OpenAiProperties =
        OpenAiProperties(mockServerContainer.endpoint, Duration.ofSeconds(1), "test-api-key", "test-model")

}