package tech.aaregall.lab.functions.question.service.openai

data class OpenAiChatCompletionRequest(
    val model: String,
    val messages: List<OpenAiMessage>
)

data class OpenAiChatCompletionResponse(
    val choices: List<OpenAiChatAnswerChoice>
)

data class OpenAiChatAnswerChoice(
    val index: Int,
    val message: OpenAiMessage
)

data class OpenAiMessage (
    val role: String,
    val content: String
)