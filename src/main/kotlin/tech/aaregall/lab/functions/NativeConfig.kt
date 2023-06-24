package tech.aaregall.lab.functions

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding
import org.springframework.context.annotation.Configuration
import tech.aaregall.lab.functions.question.domain.Answer
import tech.aaregall.lab.functions.question.domain.Question
import tech.aaregall.lab.functions.question.service.openai.OpenAiChatCompletionRequest
import tech.aaregall.lab.functions.question.service.openai.OpenAiChatCompletionResponse
import tech.aaregall.lab.functions.weather.domain.Forecast
import tech.aaregall.lab.functions.weather.domain.GeoLocation
import tech.aaregall.lab.functions.weather.service.openmeteo.OpenMeteoForecastResponse

@Configuration
@RegisterReflectionForBinding(classes = [
    GeoLocation::class,
    Forecast::class,
    Question::class,
    Answer::class,
    OpenAiChatCompletionRequest::class,
    OpenAiChatCompletionResponse::class,
    OpenMeteoForecastResponse::class
])
class NativeConfig