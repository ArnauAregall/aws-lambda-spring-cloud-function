package tech.aaregall.lab.functions.question.service

import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import tech.aaregall.lab.functions.question.domain.Answer
import tech.aaregall.lab.functions.question.domain.Question
import tech.aaregall.lab.functions.question.service.openai.OpenAiChatAnswerChoice
import tech.aaregall.lab.functions.question.service.openai.OpenAiChatCompletionResponse
import tech.aaregall.lab.functions.question.service.openai.OpenAiClient
import tech.aaregall.lab.functions.question.service.openai.OpenAiMessage

@Service
class QuestionService(val openAiClient: OpenAiClient) {

    fun answerQuestion(questions: Flux<Question>): Flux<Answer> =
        questions.map { openAiClient.chatCompletion(it.messages) }
            .flatMap { it.map(responseToAnswer()) }

    private fun responseToAnswer(): (OpenAiChatCompletionResponse) -> (Answer) = {
        Answer(it.choices.map(OpenAiChatAnswerChoice::message).map(OpenAiMessage::content).joinToString("\n"))
    }

}