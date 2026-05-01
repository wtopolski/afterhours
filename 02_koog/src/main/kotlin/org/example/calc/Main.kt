@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package org.example.calc

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTool
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResult
import ai.koog.agents.core.dsl.extension.onAssistantMessage
import ai.koog.agents.core.dsl.extension.onToolCall
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.ToolResult
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.message.Message
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.example.common.commonEventHandler
import org.example.common.ollama_model

@Serializable
@SerialName("SimpleWeatherForecast")
@LLMDescription("Simple weather forecast for a location")
data class CalcOperation(
    @property:LLMDescription("First number of operation")
    val num1: Int,
    @property:LLMDescription("Second number of operation")
    val num2: Int,
    @property:LLMDescription("The result of operation")
    val result: Int
)

val exampleOfOperation = listOf(
    CalcOperation(
        num1 = 1,
        num2 = 3,
        result = 4
    ),
    CalcOperation(
        num1 = 5,
        num2 = 5,
        result = 10
    )
)

val operationStructure = JsonStructuredData.createJsonStructure<CalcOperation>(
    schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
    examples = exampleOfOperation,
    schemaType = JsonStructuredData.JsonSchemaType.SIMPLE
)

// Create a simple strategy
val agentStrategy = strategy("Simple calculator") {
    // Define nodes for the strategy
    val nodeSendInput by nodeLLMRequest()
    val nodeExecuteTool by nodeExecuteTool()
    val nodeSendToolResult by nodeLLMSendToolResult()

    val getOperationForecast by node<Message.Response, CalcOperation> { _ -> // String
        val structuredResponse = llm.writeSession {
            this.requestLLMStructured(
                structure = operationStructure,
                fixingModel = ollama_model,
            )
        }

        structuredResponse.getOrNull()?.structure ?: CalcOperation(0,0,0)
    }

    // Define edges between nodes
    // Start -> Send input
    edge(nodeStart forwardTo nodeSendInput)

    // Send input -> Finish
    edge(
        (nodeSendInput forwardTo nodeFinish)
                transformed { it }
                onAssistantMessage { true }
    )

    // Send input -> Execute tool
    edge(
        (nodeSendInput forwardTo nodeExecuteTool)
                onToolCall { true }
    )

    // Execute tool -> Send the tool result
    edge(nodeExecuteTool forwardTo nodeSendToolResult)

    edge(nodeSendToolResult forwardTo getOperationForecast)

    edge(getOperationForecast forwardTo nodeFinish)
}

// Configure the agent
val agentConfig = AIAgentConfig(
    prompt = Prompt.build("simple-calculator") {
        system(
            """
                You are a simple calculator assistant.
                You can add and subtract two numbers together using the calculator tool.
                When the user provides input, extract the numbers they want to add.
                The input might be in various formats like "add 5 and 7", "5 + 7", or just "5 7".
                Extract the two numbers and use the calculator tool to add or subtract them.
                Always respond with a clear, friendly message showing the calculation and result.
                """.trimIndent()
        )
    },
    model = ollama_model,
    maxAgentIterations = 10
)

@Serializable
data class CalcArgs(
    val num1: Int,
    val num2: Int
) : ToolArgs

@Serializable
data class Result(
    val sum: Int
) : ToolResult {
    override fun toStringDefault(): String {
        return "The result is: $sum"
    }
}

// Implement s simple calculator tool that can add two numbers
object CalculatorPlusTool : Tool<CalcArgs, ToolResult>() {

    override val argsSerializer = CalcArgs.serializer()

    override val descriptor = ToolDescriptor(
        name = "calculator_plus",
        description = "Add two numbers together",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "num1",
                description = "First number to add",
                type = ToolParameterType.Integer
            ),
            ToolParameterDescriptor(
                name = "num2",
                description = "Second number to add",
                type = ToolParameterType.Integer
            )
        )
    )

    override suspend fun execute(args: CalcArgs): ToolResult {
        println("Perform a simple addition operation")
        val sum = args.num1 + args.num2
        return Result(sum)
    }
}

object CalculatorSubtractTool : Tool<CalcArgs, ToolResult>() {

    override val argsSerializer = CalcArgs.serializer()

    override val descriptor = ToolDescriptor(
        name = "calculator_minus",
        description = "Subtract two numbers",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "num1",
                description = "First number to subtract",
                type = ToolParameterType.Integer
            ),
            ToolParameterDescriptor(
                name = "num2",
                description = "Second number to subtract",
                type = ToolParameterType.Integer
            )
        )
    )

    override suspend fun execute(args: CalcArgs): ToolResult {
        println("Perform a simple subtract operation")
        val sum = args.num1 - args.num2
        return Result(sum)
    }
}

// Create the tool to the tool registry
val toolRegistry = ToolRegistry {
    tool(CalculatorPlusTool)
    tool(CalculatorSubtractTool)
}

// Create the agent
val agent = AIAgent(
    promptExecutor = simpleOllamaAIExecutor(),
    strategy = agentStrategy,
    agentConfig = agentConfig,
    toolRegistry = toolRegistry,
    installFeatures = { commonEventHandler() }
)

suspend fun main() {
    val response = agent.run("subtract one from nine") as CalcOperation

    println("Input: ${response.num1}")
    println("Input: ${response.num2}")
    println("Result: ${response.result}")
}