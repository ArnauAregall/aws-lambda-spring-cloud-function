package tech.aaregall.lab.functions.question.service.openai

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.support.WebClientAdapter
import org.springframework.web.service.annotation.PostExchange
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.lang.IllegalStateException
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
class OpenAiClient(private val openAiClientProeprties: OpenAiProperties, webClientBuilder: WebClient.Builder) {

    private val openAiHttpClient: OpenAiHttpClient = HttpServiceProxyFactory
        .builder(
            WebClientAdapter.forClient(
                webClientBuilder
                    .baseUrl(openAiClientProeprties.baseUrl)
                    .clientConnector(ReactorClientHttpConnector(HttpClient.create().responseTimeout(openAiClientProeprties.timeout)))
                    .defaultStatusHandler(not(HttpStatusCode::is2xxSuccessful)) {
                        Mono.error(OpenAiException("Error: OpenAI API responded with ${it.statusCode()}"))
                    }
                    .defaultHeader("Authorization", "Bearer ${openAiClientProeprties.apiKey}")
                    .build()
            )
        )
        .build()
        .createClient()

    @RegisterReflectionForBinding(classes = [OpenAiChatCompletionRequest::class, OpenAiChatCompletionResponse::class])
    fun chatCompletion(messages: List<String>): Mono<OpenAiChatCompletionResponse> =
        openAiHttpClient.chatCompletion(OpenAiChatCompletionRequest(openAiClientProeprties.model, messages.map { OpenAiMessage("user", it) }))

}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
private class OpenAiException(override val message: String?) : IllegalStateException()

private fun interface OpenAiHttpClient {

    @PostExchange("chat/completions")
    fun chatCompletion(@RequestBody openAiChatCompletionRequest: OpenAiChatCompletionRequest): Mono<OpenAiChatCompletionResponse>

}