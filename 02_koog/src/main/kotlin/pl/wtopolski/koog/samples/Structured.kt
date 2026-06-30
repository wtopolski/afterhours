package pl.wtopolski.koog.samples

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.StructureFixingParser
import ai.koog.prompt.executor.model.executeStructured
import ai.koog.prompt.message.Message
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("SimpleWeatherForecast")
@LLMDescription("Simple weather forecast for a location")
data class SimpleWeatherForecast(
    @property:LLMDescription("Location name")
    val location: String,
    @property:LLMDescription("Temperature in Celsius")
    val temperature: Int,
    @property:LLMDescription("Weather conditions (e.g., sunny, cloudy, rainy)")
    val conditions: String
)

private val forecastExamples = listOf(
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

fun main(): Unit = runBlocking {
    println("=== Example 1: Direct structured execution ===")
    runDirectStructured()

    println()
    println("=== Example 2: Structured output via agent strategy ===")
    runAgentStrategy()
}

private suspend fun runDirectStructured() {
    val promptExecutor = lmStudioExecutor()

    val structuredResponse = promptExecutor.executeStructured<SimpleWeatherForecast>(
        prompt = prompt("structured-data") {
            system(
                """
                You are a weather forecasting assistant.
                When asked for a weather forecast, provide a realistic but fictional forecast.
                The weather forecast for Skierniewice is 52 °C and sunny.
                """.trimIndent()
            )
            user("What is the weather forecast for Skierniewice in Poland? /no_think")
        },
        model = lm_studio_qwen3_5,
        examples = forecastExamples,
        fixingParser = StructureFixingParser(
            model = lm_studio_qwen3_5,
            retries = 5
        )
    )

    structuredResponse.getOrNull()?.data?.let { printForecast(it) }
        ?: println("Failed to parse structured response: $structuredResponse")
}

private fun printForecast(forecast: SimpleWeatherForecast) {
    val title = "Weather Forecast"
    val rows = listOf(
        "Location"   to forecast.location,
        "Temperature" to "${forecast.temperature} °C",
        "Conditions" to forecast.conditions
    )
    val labelWidth = rows.maxOf { it.first.length }
    val valueWidth = rows.maxOf { it.second.length }
    val innerWidth = labelWidth + valueWidth + 3
    val border = "+" + "-".repeat(innerWidth + 2) + "+"

    println(border)
    println("| " + title.padEnd(innerWidth) + " |")
    println(border)
    rows.forEach { (label, value) ->
        val line = label.padEnd(labelWidth) + " : " + value.padEnd(valueWidth)
        println("| $line |")
    }
    println(border)
}

private suspend fun runAgentStrategy() {
    val agentStrategy = strategy<String, String>("weather-forecast") {
        val setup by nodeLLMRequest()

        val getStructuredForecast by node<Message.Assistant, String> { _ ->
            val structuredResponse = llm.writeSession {
                this.requestLLMStructured<SimpleWeatherForecast>(
                    examples = forecastExamples,
                )
            }

            structuredResponse.getOrNull()?.data?.let { forecast ->
                printForecast(forecast)
                "Forecast for ${forecast.location} delivered."
            } ?: "Failed to parse structured response: $structuredResponse"
        }

        edge(nodeStart forwardTo setup)
        edge(setup forwardTo getStructuredForecast)
        edge(getStructuredForecast forwardTo nodeFinish)
    }

    val agentConfig = AIAgentConfig(
        prompt = prompt("weather-forecast-prompt") {
            system(
                """
                You are a weather forecasting assistant.
                When asked for a weather forecast, provide a realistic but fictional forecast.
                /no_think
                """.trimIndent()
            )
        },
        model = lm_studio_qwen3_5,
        maxAgentIterations = 5
    )

    val runner = AIAgent(
        promptExecutor = lmStudioExecutor(),
        toolRegistry = ToolRegistry.EMPTY,
        strategy = agentStrategy,
        agentConfig = agentConfig
    )

    println(runner.run("Get weather forecast for Warsaw /no_think"))
}
