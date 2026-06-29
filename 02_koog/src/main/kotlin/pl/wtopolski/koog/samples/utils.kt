package pl.wtopolski.koog.samples

import ai.koog.agents.core.agent.GraphAIAgent.FeatureContext
import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
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