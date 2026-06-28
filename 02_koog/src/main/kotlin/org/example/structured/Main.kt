package org.example.structured

import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.structure.executeStructured
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import kotlinx.coroutines.runBlocking
import org.example.common.ollama_model
import ai.koog.agents.core.tools.annotations.LLMDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

fun main(): Unit = runBlocking {
    val weatherForecastStructure = JsonStructuredData.createJsonStructure<SimpleWeatherForecast>(
        schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
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
        schemaType = JsonStructuredData.JsonSchemaType.SIMPLE
    )

    val promptExecutor = simpleOllamaAIExecutor()

    // Make an LLM call that returns a structured response
    val structuredResponse = promptExecutor.executeStructured(
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
        // Provide the expected data structure to the LLM
        structure = weatherForecastStructure,
        // Define the main model that will execute the request
        mainModel = ollama_model,
        // Set the maximum number of retries to get a proper structured response
        retries = 5,
        // Set the LLM used for output coercion (transformation of malformed outputs)
        fixingModel = ollama_model
    )

    println(structuredResponse)
    println(structuredResponse.getOrNull()?.structure)
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