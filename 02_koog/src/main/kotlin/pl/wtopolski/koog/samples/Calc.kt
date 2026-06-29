@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package pl.wtopolski.koog.samples

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.ReceivedToolResults
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMProvider
import ai.koog.serialization.JSONPrimitive
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private val lmStudioExecutor = MultiLLMPromptExecutor(
    LLMProvider.OpenAI to OpenAILLMClient(
        apiKey = "lm-studio",
        settings = OpenAIClientSettings(baseUrl = LM_STUDIO_BASE_URL)
    )
)

@Serializable
@SerialName("CalcOperation")
@LLMDescription("Calculator operation result")
data class CalcOperation(
    @property:LLMDescription("First number of operation")
    val num1: Int,
    @property:LLMDescription("Second number of operation")
    val num2: Int,
    @property:LLMDescription("The result of operation")
    val result: Int
)

val exampleOfOperation = listOf(
    CalcOperation(num1 = 1, num2 = 3, result = 4),
    CalcOperation(num1 = 5, num2 = 5, result = 10)
)

// Annotation-based tool definition
@Suppress("unused")
@LLMDescription("Tools for basic calculator operations")
class CalculatorTools : ToolSet {

    @Tool
    @LLMDescription("Add two numbers together")
    fun calculatorPlus(
        @LLMDescription("First number to add")
        num1: Int,
        @LLMDescription("Second number to add")
        num2: Int
    ): String {
        println("Perform a simple addition operation")
        return "The result is: ${num1 + num2}"
    }

    @Tool
    @LLMDescription("Subtract two numbers")
    fun calculatorMinus(
        @LLMDescription("First number to subtract")
        num1: Int,
        @LLMDescription("Second number to subtract")
        num2: Int
    ): String {
        println("Perform a simple subtract operation")
        return "The result is: ${num1 - num2}"
    }
}

// Create a simple strategy
val calcAgentStrategy = strategy<String, Any>("Simple calculator") {
    val nodeSendInput by nodeLLMRequest()
    val nodeExecuteTool by nodeExecuteTools()
    // Parse CalcOperation directly from tool results — avoids a second LLM call,
    // which fails on LM Studio/Qwen because its jinja template can't handle
    // the assistant tool_calls message in history followed by non-tool messages.
    val nodeParseResult by node<ReceivedToolResults, CalcOperation> { toolResults ->
        val result = toolResults.toolResults.firstOrNull() ?: error("No tool result found")
        val num1 = (result.toolArgs.entries["num1"] as? JSONPrimitive)?.intOrNull ?: 0
        val num2 = (result.toolArgs.entries["num2"] as? JSONPrimitive)?.intOrNull ?: 0
        val match = Regex("(-?\\d+)").findAll(result.output).lastOrNull()
        val resultValue = match?.value?.toIntOrNull() ?: 0
        CalcOperation(num1, num2, resultValue)
    }

    edge(nodeStart forwardTo nodeSendInput)

    edge(
        (nodeSendInput forwardTo nodeFinish)
                onTextMessage { true }
    )

    edge(
        (nodeSendInput forwardTo nodeExecuteTool)
                onToolCalls { true }
    )

    edge(nodeExecuteTool forwardTo nodeParseResult)

    edge(nodeParseResult forwardTo nodeFinish)
}

// Configure the agent
val agentConfig = AIAgentConfig(
    prompt = prompt("simple-calculator") {
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
    model = lm_studio_qwen3_5,
    maxAgentIterations = 10
)

// Create the tool registry
val toolRegistry = ToolRegistry {
    tools(CalculatorTools().asTools())
}

// Create the agent
val agent = AIAgent(
    promptExecutor = lmStudioExecutor,
    strategy = calcAgentStrategy,
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