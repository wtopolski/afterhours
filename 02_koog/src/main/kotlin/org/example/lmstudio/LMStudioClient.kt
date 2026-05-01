package org.example.lmstudio

import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.prompt.dsl.ModerationResult
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.clients.LLMClient
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import kotlinx.coroutines.flow.Flow
import ai.koog.prompt.message.ResponseMetaInfo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class LMStudioClient(
    val baseUrl: String,
    val clock: Clock = Clock.System
) : LLMClient {

    override suspend fun execute(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>
    ): List<Message.Response> {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                }) // uses kotlinx.serialization
            }
        }

        val messages = prompt.messages.map {
            val role = when (it.role) {
                Message.Role.System -> "system"
                Message.Role.User -> "user"
                Message.Role.Assistant -> "assistant"
                Message.Role.Tool -> "tool"
            }
            ChatMessage(role, it.content)
        }

        val rawResponse: HttpResponse = client.post("$baseUrl/api/v0/chat/completions") {
            contentType(ContentType.Application.Json)
            setBody(
                ChatRequest(
                    model = "google/gemma-3-12b",
                    messages = messages,
                    temperature = 0.7,
                    max_tokens = -1,
                    stream = false
                )
            )
        }
        val response = rawResponse.body<ChatResponse>()
        client.close()

        val content = response.choices.get(0).message.content

        // Get token counts from the response, or use null if not available
        val promptTokenCount = response.usage.prompt_tokens
        val responseTokenCount = response.usage.completion_tokens
        val totalTokensCount = response.usage.total_tokens

        val responseMetadata = ResponseMetaInfo.create(
            clock,
            totalTokensCount = totalTokensCount,
            inputTokensCount = promptTokenCount,
            outputTokensCount = responseTokenCount,
        )

        return listOf(
            Message.Assistant(
                content = content, metaInfo = responseMetadata
            )
        )
    }

    override fun executeStreaming(
        prompt: Prompt,
        model: LLModel
    ): Flow<String> {
        TODO("Not yet implemented")
    }

    override suspend fun moderate(
        prompt: Prompt,
        model: LLModel
    ): ModerationResult {
        TODO("Not yet implemented")
    }
}