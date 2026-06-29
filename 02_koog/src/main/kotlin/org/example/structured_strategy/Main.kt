package org.example.structured_strategy

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.message.Message
import kotlinx.coroutines.runBlocking
import pl.wtopolski.koog.samples.commonEventHandler
import pl.wtopolski.koog.samples.ollama_model
import pl.wtopolski.koog.samples.ollamaExecutor
import org.example.structured.SimpleWeatherForecast

fun main(): Unit = runBlocking {
    val forecastExamples = listOf(
        SimpleWeatherForecast(
            location = "New York",
            temperature = 25,
            conditions = "Sunny"
        ),
        SimpleWeatherForecast(
            location = "London",
            temperature = 18,
            conditions = "Cloudy"
        )
    )

    // Define the agent strategy
    val agentStrategy = strategy<String, String>("weather-forecast") {
        val setup by nodeLLMRequest()

        val getStructuredForecast by node<Message.Assistant, String> { _ ->
            val structuredResponse = llm.writeSession {
                this.requestLLMStructured<SimpleWeatherForecast>(
                    examples = forecastExamples,
                )
            }

            """
            Response structure:
            $structuredResponse
            """.trimIndent()
        }

        edge(nodeStart forwardTo setup)
        edge(setup forwardTo getStructuredForecast)
        edge(getStructuredForecast forwardTo nodeFinish)
    }

    // Configure and run the agent
    val agentConfig = AIAgentConfig(
        prompt = prompt("weather-forecast-prompt") {
            system(
                """
                You are a weather forecasting assistant.
                When asked for a weather forecast, provide a realistic but fictional forecast.
                """.trimIndent()
            )
        },
        model = ollama_model,
        maxAgentIterations = 5
    )

    val runner = AIAgent(
        promptExecutor = ollamaExecutor(),
        toolRegistry = ToolRegistry.EMPTY,
        strategy = agentStrategy,
        agentConfig = agentConfig,
        installFeatures = { commonEventHandler() }
    )

    println(runner.run("Get weather forecast for Warsaw"))
}