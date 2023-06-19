package tech.aaregall.lab.functions.question

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import reactor.core.publisher.Mono
import tech.aaregall.lab.functions.question.domain.Answer
import tech.aaregall.lab.functions.question.domain.Question
import tech.aaregall.lab.functions.question.service.QuestionService

@Configuration
@RegisterReflectionForBinding(classes = [Question::class, Answer::class])
class QuestionFunctions {

    @Bean
    fun question(questionService: QuestionService): (Message<Question>) -> Mono<Answer> = questionService::answerQuestion

}