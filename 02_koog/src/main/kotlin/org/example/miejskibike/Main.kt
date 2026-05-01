@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package org.example.miejskibike

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.message.Message
import org.example.common.ollama_model

// Create a simple strategy
val agentStrategy = strategy("Bike points Location Assistant of miejski.bike service") {
    // Define nodes for the strategy
    val nodeSendInput by nodeLLMRequest()
    val nodeExecuteTool by nodeExecuteTool()
    val nodeSendToolResult by nodeLLMSendToolResult()

    val getOperationForecast by node<Message.Response, ResponseDescription> { _ ->
        val structuredResponse = llm.writeSession {
            this.requestLLMStructured(
                structure = bikePointsStructure,
                fixingModel = ollama_model,
            )
        }

        structuredResponse.getOrNull()?.structure ?: ResponseDescription()
    }

    edge(
        nodeStart forwardTo nodeSendInput
    )

    edge(
        (nodeSendInput forwardTo nodeFinish) transformed { it } onAssistantMessage { true }
    )

    edge(
        (nodeSendInput forwardTo nodeExecuteTool) onToolCall { true }
    )

    edge(
        nodeExecuteTool forwardTo nodeSendToolResult
    )

    edge(
        nodeSendToolResult forwardTo getOperationForecast
    )

    edge(
        getOperationForecast forwardTo nodeFinish
    )
}

suspend fun main() {
    val agentConfig = AIAgentConfig(
        prompt = Prompt.build("miejski-bike") {
            system(
                """
                You are a Bike Points Location Assistant. 
                
                You provide details for specific bike points locations. When the user requests a location or city, extract its ID. 
                The response includes all fetched data about the points.
                
                Use only data from registered tools.
                """.trimIndent()
            )
        },
        model = ollama_model,
        maxAgentIterations = 10
    )

    // Create the tool to the tool registry
    val toolRegistry = ToolRegistry {
        tools(listOf(SayToUser, MiejskiBikeTool))
    }

    // Create the agent
    val agent = AIAgent(
        promptExecutor = simpleOllamaAIExecutor(),
        strategy = agentStrategy,
        agentConfig = agentConfig,
        toolRegistry = toolRegistry
    )

//    val resultA = agent.run("""Where can I park my bike in Poznań use data from miejski bike service, give me addresses or names!""")
//    println(resultA)

    val result = agent.run("""Find all workshops where I can fix my bike in Poznan use data from miejski bike service, give me addresses!""")
    if (result is ResponseDescription) {
        println("Type: ${result.bikeType} from ${result.zoneName}, number of points: ${result.numberOfPoint}")
        println("------------------------------")
        result.bikePoints.forEach {
            println(it)
        }
    }
}