package org.example.common

import ai.koog.agents.core.agent.AIAgent.FeatureContext
import ai.koog.agents.features.eventHandler.feature.EventHandler
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel

val llama3_2 = LLModel(
    provider = LLMProvider.Ollama,
    id = "llama3.2",
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Simple,
        LLMCapability.Tools
    )
)

val gpt_oss_20b = LLModel(
    provider = LLMProvider.Ollama,
    id = "gpt-oss:20b",
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Schema.JSON.Simple,
        LLMCapability.Tools
    )
)

val ollama_model = gpt_oss_20b // OllamaModels.Meta.LLAMA_3_2

fun FeatureContext.commonEventHandler() {
    install(EventHandler) {
        onBeforeAgentStarted { context ->
            println("On Before Agent Started: $context")
            println("")
        }
        onAgentFinished { context ->
            println("On Agent Finished: $context")
            println("")
        }
        onToolCall { context ->
            println("On Tool Call: $context")
            println("")
        }
        onAfterLLMCall { context ->
            println("On After LLM Call: $context")
            println("")
        }
        onBeforeAgentStarted { context ->
            println("On Before Agent Started: $context")
            println("")
        }
        onAfterNode { context ->
            println("On After Node: $context")
            println("")
        }
        onBeforeNode { context ->
            println("On Before Node: $context")
            println("")
        }
        onAgentRunError { context ->
            println("On Agent Run Error: $context")
            println("")
        }
    }
}