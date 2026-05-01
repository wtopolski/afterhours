@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package org.example.basic

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import kotlinx.coroutines.runBlocking
import org.example.common.ollama_model

fun main(): Unit = runBlocking {

    val toolRegistry = ToolRegistry {
        tools(
            listOf(SayToUser)
        )
    }

    val agent = AIAgent(
        executor = simpleOllamaAIExecutor(),
        systemPrompt = "You are a helpful assistant. Answer user questions concisely.",
        toolRegistry = toolRegistry,
        llmModel = ollama_model
    )

    agent.run("Hello, how can you help me?")
}