package tech.aaregall.lab.functions.question.service.openai

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.function.Predicate.not

@ConfigurationProperties("app.openai")
data class OpenAiProperties (
    val baseUrl: String,
    val timeout: Duration,
    val apiKey: String,
    val model: String
)

@Component
class OpenAiClient(private val openAiClientProperties: OpenAiProperties, webClientBuilder: WebClient.Builder) {

    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    private val openAiHttpClient: OpenAiHttpClient = HttpServiceProxyFactory
        .builder(
            WebClientAdapter.forClient(
                webClientBuilder
                    .baseUrl(openAiClientProperties.baseUrl)
                    .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(openAiClientProperties.timeout)))
                    .defaultHeader("Authorization", "Bearer ${openAiClientProperties.apiKey}")
                    .defaultStatusHandler(not(HttpStatusCode::is2xxSuccessful)) {
                        Mono.error(OpenAiException(it.statusCode()))
                    }
                    .build()
            )
        )
        .build()
        .createClient()

    fun chatCompletion(messages: List<String>): Flux<OpenAiChatCompletionResponse> =
        openAiHttpClient.chatCompletion(OpenAiChatCompletionRequest(openAiClientProperties.model, messages.map { OpenAiMessage("user", it) }))
            .doOnError { logger.error("An error occurred while calling OpenAI for chat completion", it) }

}

private class OpenAiException(
    status: HttpStatusCode,
    message: String? = "Error: OpenAI API responded with $status") : ResponseStatusException(status, message)

private fun interface OpenAiHttpClient {

    @PostExchange("chat/completions")
    fun chatCompletion(@RequestBody openAiChatCompletionRequest: OpenAiChatCompletionRequest): Flux<OpenAiChatCompletionResponse>

}