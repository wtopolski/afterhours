package pl.wtopolski.koog.samples

import ai.koog.agents.core.agent.GraphAIAgent.FeatureContext
import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.http.client.HttpClientFactoryResolver
import ai.koog.prompt.Prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.message.Message
import ai.koog.prompt.message.MessagePart
import ai.koog.serialization.typeToken
import kotlinx.serialization.Serializable


const val LM_STUDIO_BASE_URL = "http://localhost:1234"
const val LM_STUDIO_MODEL_ID = "qwen/qwen3.5-35b-a3b"

val lm_studio_qwen3_5 = LLModel(
    provider = LLMProvider.OpenAI,
    id = LM_STUDIO_MODEL_ID,
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Basic,
        LLMCapability.Tools,
        LLMCapability.Completion,
        LLMCapability.OpenAIEndpoint.Completions
    )
)

val gpt_oss_20b = LLModel(
    provider = LLMProvider.Ollama,
    id = "gpt-oss:20b",
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Basic,
        LLMCapability.Tools
    )
)

val ollama_model = gpt_oss_20b // OllamaModels.Meta.LLAMA_3_2

fun FeatureContext.commonEventHandler() {
    handleEvents {
        onAgentStarting { context ->
            println("On Agent Starting: $context")
            println("")
        }
        onAgentCompleted { context ->
            println("On Agent Completed: $context")
            println("")
        }
        onToolCallStarting { context ->
            println("On Tool Call Starting: $context")
            println("")
        }
        onLLMCallCompleted { context ->
            println("On LLM Call Completed: $context")
            println("")
        }
        onNodeExecutionCompleted { context ->
            println("On Node Execution Completed: $context")
            println("")
        }
        onNodeExecutionStarting { context ->
            println("On Node Execution Starting: $context")
            println("")
        }
        onAgentExecutionFailed { context ->
            println("On Agent Execution Failed: $context")
            println("")
        }
    }
}

object SayToUser : SimpleTool<SayToUser.Args>(
    argsType = typeToken<Args>(),
    name = "say_to_user",
    description = "Service tool, used by the agent to talk."
) {
    @Serializable
    data class Args(val message: String)

    override suspend fun execute(args: Args): String {
        println(args.message)
        return "Message delivered to user"
    }
}

fun ollamaExecutor(baseUrl: String = "http://localhost:11434"): MultiLLMPromptExecutor =
    MultiLLMPromptExecutor(LLMProvider.Ollama to OllamaClient(baseUrl = baseUrl))

/**
 * LM Studio routes Qwen "thinking" model output into `reasoning_content` and leaves `content`
 * empty. That makes koog's structured-output parser blow up because it calls `.single()` on
 * `MessagePart.Text`. This client promotes the reasoning text into a Text part when no Text
 * part is present so structured parsing can succeed.
 */
class LmStudioOpenAIClient(
    apiKey: String = "lm-studio",
    settings: OpenAIClientSettings = OpenAIClientSettings(baseUrl = LM_STUDIO_BASE_URL)
) : OpenAILLMClient(
    apiKey = apiKey,
    settings = settings,
    httpClientFactory = HttpClientFactoryResolver.resolve()
) {

    override suspend fun execute(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>
    ): Message.Assistant {
        val response = super.execute(prompt, model, tools)
        return promoteReasoningToText(response)
    }

    private fun promoteReasoningToText(response: Message.Assistant): Message.Assistant {
        val hasText = response.parts.any { it is MessagePart.Text }
        if (hasText) return response
        val reasoningText = response.parts
            .filterIsInstance<MessagePart.Reasoning>()
            .flatMap { it.content }
            .joinToString("")
            .ifBlank { return response }
        val newParts = response.parts.filterNot { it is MessagePart.Reasoning } +
            MessagePart.Text(reasoningText)
        return response.copy(parts = newParts)
    }
}

fun lmStudioExecutor(): MultiLLMPromptExecutor =
    MultiLLMPromptExecutor(LLMProvider.OpenAI to LmStudioOpenAIClient())