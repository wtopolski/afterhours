@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package org.example.lmstudio

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val agent = AIAgent(
        executor = SingleLLMPromptExecutor(LMStudioClient("https://5848-2a02-a31a-20d9-5600-bc13-2f5-ce1f-f110.ngrok-free.app")),
        systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
        llmModel = LLModel(
            provider = LLMProvider.Google,
            id = "google/gemma-3-12b",
            capabilities = listOf(
                LLMCapability.Temperature,
                LLMCapability.Schema.JSON.Simple,
                LLMCapability.Tools
            )
        )
    )

    println(agent.run("What's the weather like in Warsaw?"))
}