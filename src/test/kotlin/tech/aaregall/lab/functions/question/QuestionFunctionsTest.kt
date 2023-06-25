package tech.aaregall.lab.functions.question

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.reactive.server.WebTestClient
import tech.aaregall.lab.functions.question.service.openai.OpenAiProperties

@SpringBootTest
@ExtendWith(OutputCaptureExtension::class)
class QuestionFunctionsTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val mockServerClient: MockServerClient,
    @Autowired val openAiProperties: OpenAiProperties) {

    @Nested
    inner class Question {

        @ParameterizedTest
        @ValueSource(ints = [400, 401, 404, 503, 504])
        fun `When OpenAI API returns client or server errors, then returns empty and error is handled`(statusCode: Int, capturedOutput: CapturedOutput) {
            mockServerClient.reset()
                .`when`(
                    request()
                    .withMethod(POST.name())
                    .withPath("/chat/completions")
                    .withHeader("Authorization", "Bearer ${openAiProperties.apiKey}")
                ).respond(response().withStatusCode(statusCode))

            webTestClient.post().uri("/question")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .bodyValue("""
                    [
                        {"messages": ["Could you please say hello?"]}
                    ]
                """.trimIndent())
                .exchange()
                .expectHeader().contentType(APPLICATION_JSON_VALUE)
                .expectStatus().is2xxSuccessful // limitation on Spring Cloud Function Webflux not being able to control response header
                .expectBody()
                .jsonPath("$").isArray
                .jsonPath("$").isEmpty

            assertThat(capturedOutput)
                .contains("An error occurred while calling OpenAI for chat completion")
                .contains(statusCode.toString())
        }

        @Test
        fun `When OpenAI API returns results, then returns OK and response contains Answer`() {
            mockServerClient.reset()
                .`when`(
                    request()
                        .withMethod(POST.name())
                        .withPath("/chat/completions")
                        .withHeader("Authorization", "Bearer ${openAiProperties.apiKey}")
                ).respond(
                    response()
                        .withStatusCode(HttpStatus.OK.value())
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                        .withBody("""
                            {
                              "id": "chatcmpl-123",
                              "object": "chat.completion",
                              "created": 1677652288,
                              "choices": [
                                {
                                    "index": 0,
                                    "message": {
                                        "role": "assistant",
                                        "content": "Hello there, glad you are so polite!"
                                    }
                                },
                                {
                                    "index": 1,
                                    "message": {
                                        "role": "assistant",
                                        "content": "How may I assist you today?"
                                    },
                                    "finish_reason": "stop"
                                }
                              ],
                              "usage": {
                                "prompt_tokens": 9,
                                "completion_tokens": 12,
                                "total_tokens": 21
                              }
                            }
                        """.trimIndent()))

            webTestClient
                .post()
                .uri("/question")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .bodyValue("""
                    {"messages": ["Could you please say hello?", "Thank you!"]}
                """.trimIndent())
                .exchange()
                .expectHeader().contentType(APPLICATION_JSON_VALUE)
                .expectStatus().is2xxSuccessful
                .expectBody()
                .jsonPath("$").isArray
                .jsonPath("$").isNotEmpty
                .jsonPath("$[0].answer").isEqualTo("Hello there, glad you are so polite!\nHow may I assist you today?")
        }

    }

}