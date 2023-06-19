package tech.aaregall.lab.functions.question

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EmptySource
import org.junit.jupiter.params.provider.ValueSource
import org.mockserver.client.MockServerClient
import org.mockserver.model.HttpRequest.request
import org.mockserver.model.HttpResponse.response
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.HttpMethod.POST
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.test.web.reactive.server.WebTestClient
import tech.aaregall.lab.functions.question.service.openai.OpenAiProperties

@SpringBootTest
class QuestionFunctionsTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired val mockServerClient: MockServerClient,
    @Autowired val openAiProperties: OpenAiProperties) {

    @Nested
    inner class Question {

        @ParameterizedTest
        @EmptySource
        fun `When body is empty then returns server error`(body: String) {
            webTestClient.post().uri("/question")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .bodyValue(body)
                .exchange()
                .expectHeader().contentType(APPLICATION_JSON_VALUE)
                .expectStatus().is5xxServerError
        }

        @ParameterizedTest
        @ValueSource(ints = [400, 401, 404, 503, 504])
        fun `When OpenAI API returns client or server errors then returns server error`(statusCode: Int) {
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
                    {"messages": ["Could you please say hello?"]}
                """.trimIndent())
                .exchange()
                .expectHeader().contentType(APPLICATION_JSON_VALUE)
                .expectStatus().is5xxServerError
        }

        @Test
        fun `When OpenAI API returns results then returns OK and response contains Answer`() {
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
                .jsonPath("$.answer").isNotEmpty
                .jsonPath("$.answer").isEqualTo("Hello there, glad you are so polite!\nHow may I assist you today?")
        }

    }

}