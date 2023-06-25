package tech.aaregall.lab.functions.question

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import tech.aaregall.lab.functions.question.domain.Answer
import tech.aaregall.lab.functions.question.domain.Question
import tech.aaregall.lab.functions.question.service.QuestionService

@Configuration
class QuestionFunctions {

    @Bean
    fun question(questionService: QuestionService): (Flux<Question>) -> Flux<Answer> = questionService::answerQuestion

}