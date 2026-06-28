@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package org.example.basic

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import kotlinx.coroutines.runBlocking
import org.example.common.LM_STUDIO_BASE_URL
import org.example.common.lm_studio_qwen3_5


fun main(): Unit = runBlocking {

    val toolRegistry = ToolRegistry {
        tools(
            listOf(SayToUser)
        )
    }

    val lmStudioClient = OpenAILLMClient(
        apiKey = "lm-studio",
        settings = OpenAIClientSettings(baseUrl = LM_STUDIO_BASE_URL)
    )

    val agent = AIAgent(
        executor = SingleLLMPromptExecutor(lmStudioClient),
        systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
        toolRegistry = toolRegistry,
        llmModel = lm_studio_qwen3_5
    )

    agent.run("Hello, how can you help me?")
}