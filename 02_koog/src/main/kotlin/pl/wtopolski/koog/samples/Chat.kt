@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package pl.wtopolski.koog.samples

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.message.MessagePart
import kotlinx.coroutines.runBlocking

fun main(): Unit = runBlocking {
    val lmStudioClient = OpenAILLMClient(
        apiKey = "lm-studio",
        settings = OpenAIClientSettings(baseUrl = LM_STUDIO_BASE_URL)
    )

    val chatStrategy = strategy<String, String>("chat") {
        val chat by node<String, String> { firstMessage ->
            var userInput = firstMessage
            while (true) {
                val response = llm.writeSession {
                    appendPrompt { user(userInput) }
                    requestLLM()
                }
                val text = response.parts
                    .filterIsInstance<MessagePart.Text>()
                    .joinToString("") { it.text }
                println("\nAssistant: $text\n")

                print("You: ")
                userInput = readLine() ?: break
                if (userInput == "/exit") break
            }
            "Chat ended"
        }

        edge(nodeStart forwardTo chat)
        edge(chat forwardTo nodeFinish)
    }

    val agentConfig = AIAgentConfig(
        prompt = prompt("chat-prompt") {
            system("You are a helpful assistant. Answer user questions concisely.")
        },
        model = lm_studio_qwen3_5,
        maxAgentIterations = 1000
    )

    val agent = AIAgent(
        promptExecutor = MultiLLMPromptExecutor(LLMProvider.OpenAI to lmStudioClient),
        strategy = chatStrategy,
        agentConfig = agentConfig,
        toolRegistry = ToolRegistry.EMPTY
    )

    print("You: ")
    val firstMessage = readLine() ?: return@runBlocking
    agent.run(firstMessage)
}