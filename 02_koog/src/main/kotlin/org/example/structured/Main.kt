package org.example.structured

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.model.StructureFixingParser
import ai.koog.prompt.executor.model.executeStructured
import kotlinx.coroutines.runBlocking
import pl.wtopolski.koog.samples.ollama_model
import pl.wtopolski.koog.samples.ollamaExecutor
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun main(): Unit = runBlocking {
    val promptExecutor = ollamaExecutor()

    // Make an LLM call that returns a structured response
    val structuredResponse = promptExecutor.executeStructured<SimpleWeatherForecast>(
        // Define the prompt (both system and user messages)
        prompt = prompt("structured-data") {
            system(
                """
                You are a weather forecasting assistant.
                When asked for a weather forecast, provide a realistic but fictional forecast.
                """.trimIndent()
            )
            user(
                "What is the weather forecast for Skierniewice in Poland?"
            )
        },
        // Define the main model that will execute the request
        model = ollama_model,
        // Provide examples to help the LLM understand the expected format
        examples = listOf(
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
        ),
        // Optional fixing parser for malformed responses
        fixingParser = StructureFixingParser(
            model = ollama_model,
            retries = 5
        )
    )

    println(structuredResponse)
    println(structuredResponse.getOrNull()?.data)
}

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