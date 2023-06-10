package tech.aaregall.lab.functions.test

import org.mockserver.client.MockServerClient
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.MockServerContainer
import org.testcontainers.utility.DockerImageName

@Configuration
class TestConfig {

    @Bean
    fun webTestClient(applicationContext: ApplicationContext): WebTestClient =
        WebTestClient.bindToApplicationContext(applicationContext).build()

    @Configuration
    class MockServerConfig {

        @Bean
        fun mockServerContainer(): MockServerContainer = MockServerContainer(
            DockerImageName.parse("mockserver/mockserver")
                .withTag(MockServerClient::class.java.`package`.implementationVersion))

        @Bean
        fun mockServerClient(mockServerContainer: MockServerContainer): MockServerClient =
            MockServerClient(mockServerContainer.host, mockServerContainer.serverPort)

    }

}