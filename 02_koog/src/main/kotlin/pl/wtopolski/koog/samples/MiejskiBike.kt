@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package pl.wtopolski.koog.samples

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.annotation.InternalAgentsApi
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls
import ai.koog.agents.core.dsl.extension.ReceivedToolResults
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.Prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMProvider
import ai.koog.serialization.JSONPrimitive
import ai.koog.serialization.typeToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.port
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.request
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val lmStudioExecutor = MultiLLMPromptExecutor(
    LLMProvider.OpenAI to OpenAILLMClient(
        apiKey = "lm-studio",
        settings = OpenAIClientSettings(baseUrl = LM_STUDIO_BASE_URL)
    )
)

// Create a simple strategy
val mbAgentStrategy = strategy<String, Any>("Bike points Location Assistant of miejski.bike service") {
    val nodeSendInput by nodeLLMRequest()
    val nodeExecuteTool by nodeExecuteTools()
    // Parse ResponseDescription directly from tool results — avoids a second LLM call,
    // which fails on LM Studio/Qwen because its jinja template can't handle tool_calls history.
    @OptIn(InternalAgentsApi::class)
    val nodeParseResult by node<ReceivedToolResults, ResponseDescription> { toolResults ->
        val toolResult = toolResults.toolResults.firstOrNull() ?: error("No tool result found")
        val zoneId = (toolResult.toolArgs.entries["zoneId"] as? JSONPrimitive)?.content ?: ""
        val typeId = (toolResult.toolArgs.entries["typeId"] as? JSONPrimitive)?.content ?: ""
        val bikeResult = toolResult.resultObject as? BikeResult
        ResponseDescription(
            bikeType = typeId,
            zoneName = zoneId,
            numberOfPoint = bikeResult?.content?.size ?: 0,
            bikePoints = bikeResult?.content?.map {
                BikePointDescription(it.name, it.locationLat, it.locationLng)
            } ?: emptyList()
        )
    }

    edge(nodeStart forwardTo nodeSendInput)

    edge(
        (nodeSendInput forwardTo nodeFinish) onTextMessage { true }
    )

    edge(
        (nodeSendInput forwardTo nodeExecuteTool) onToolCalls { true }
    )

    edge(nodeExecuteTool forwardTo nodeParseResult)

    edge(nodeParseResult forwardTo nodeFinish)
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
        model = lm_studio_qwen3_5,
        maxAgentIterations = 10
    )

    // Create the tool to the tool registry
    val toolRegistry = ToolRegistry {
        tools(listOf(SayToUser, MiejskiBikeTool))
    }

    // Create the agent
    val agent = AIAgent(
        promptExecutor = lmStudioExecutor,
        strategy = mbAgentStrategy,
        agentConfig = agentConfig,
        toolRegistry = toolRegistry
    )

//    val resultA = agent.run("""Where can I park my bike in Poznań use data from miejski bike service, give me addresses or names!""")
//    println(resultA)

    val result =
        agent.run("""Find all workshops where I can fix my bike in Poznan use data from miejski bike service, give me addresses!""")
    if (result is ResponseDescription) {
        println("Type: ${result.bikeType} from ${result.zoneName}, number of points: ${result.numberOfPoint}")
        println("------------------------------")
        result.bikePoints.forEach {
            println(it)
        }
    }
}

object MiejskiBikeTool : Tool<BikeRequestArgs, BikeResult>(
    argsType = typeToken<BikeRequestArgs>(),
    resultType = typeToken<BikeResult>(),
    descriptor = ToolDescriptor(
        name = "miejski_bike_content",
        description = "Provide details like names or addresses for specific bike points from miejski.bike service",
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "zoneId",
                description = "Zone name in Polish, it could be city name like poznan or some area like trojmiasto",
                type = ToolParameterType.Enum(entries = ZoneTypes.entries)
            ),
            ToolParameterDescriptor(
                name = "typeId",
                description = "Type of bike points like wrench, rack or workshop",
                type = ToolParameterType.Enum(entries = PointTypes.entries)
            )
        )
    )
) {
    override suspend fun execute(args: BikeRequestArgs): BikeResult {
        val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }

        val httpResponse: HttpResponse = client.get(
            urlString = "https://storage.miejski.bike",
            block = {
                headers.append(HttpHeaders.ContentType, "application/json")
                headers.append(HttpHeaders.UserAgent, "AI Agent")
                headers.append(HttpHeaders.AcceptLanguage, "PL-pl")
                headers.append(HttpHeaders.Authorization, "...")
                port = 8444
                url {
                    appendPathSegments("api", "v1", "zones", args.zoneId.lowercase(), "points")
                }
            }
        )

        try {
            val typeId = "${args.typeId}_${args.zoneId}".lowercase()
            println("execute ----------> args: $args request: " + httpResponse.request.url)

            val response = httpResponse.body<List<DTOBikePoint>>()
                .filter { bikePoint -> bikePoint.typeId == typeId }
                .take(10)
                .map { point ->
                    val name = point.attrs.firstOrNull() { it.key == "name" }?.value
                    val address = point.attrs.firstOrNull() { it.key == "address" }?.value
                    val finalName = name ?: address ?: "no name"
                    BikePoint(name = finalName, locationLat = point.location.lat, locationLng = point.location.lng)
                }

            client.close()

            println("execute ----------> zoneId: " + args.zoneId + " size: " + response.size)

            response.forEach {
                println("point: $it")
            }

            return BikeResult(response)
        } catch (error: Exception) {
            println("execute ----------> error: $error")
            return BikeResult(emptyList())
        }
    }
}

@Serializable
data class BikeRequestArgs(
    val zoneId: String,
    val typeId: String
)

@Serializable
data class BikeResult(
    val content: List<BikePoint>
)

@Serializable
data class BikePoint(
    val name: String,
    val locationLat: Double,
    val locationLng: Double
)

@Serializable
data class DTOBikePoint(
    val id: String,
    val zoneId: String,
    val typeId: String,
    val location: Location,
    val attrs: List<Attr>
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)

@Serializable
data class Attr(
    val key: String,
    val value: String,
    val lang: String,
    val tag: Boolean? = null
)

@Serializable
enum class PointTypes {
    rack, wrench, workshop
}

@Serializable
enum class ZoneTypes {
    lodz, warszawa, poznan
}

// Create sample forecasts
val exampleForecasts = listOf(
    ResponseDescription(
        bikeType = "workshop",
        zoneName = "warszawa",
        numberOfPoint = 1,
        bikePoints = listOf(
            BikePointDescription(
                name = "Mój rower",
                locationLat = 50.02,
                locationLng = 22.8
            )
        )
    ),
    ResponseDescription(
        bikeType = "wrench",
        zoneName = "lodz",
        numberOfPoint = 2,
        bikePoints = listOf(
            BikePointDescription(
                name = "Mój rower",
                locationLat = 50.02,
                locationLng = 22.8
            ),
            BikePointDescription(
                name = "Mój rower 2",
                locationLat = 40.02,
                locationLng = 28.8
            )
        )
    ),
    ResponseDescription(
        bikeType = "rack",
        zoneName = "poznan",
        numberOfPoint = 0
    )
)

// Examples for structured output
val bikePointsExamples = exampleForecasts

@Serializable
@SerialName("BikePointDescription")
@LLMDescription("Description of response from miejski bike service")
data class ResponseDescription(
    @property:LLMDescription("Type of points that are requested")
    val bikeType: String = "",
    @property:LLMDescription("Name of zone that are requested")
    val zoneName: String = "",
    @property:LLMDescription("Number of found bike points from response from miejski bike")
    val numberOfPoint: Int = 0,
    @property:LLMDescription("List of bike points from response from miejski bike")
    val bikePoints: List<BikePointDescription> = emptyList(),
)

@Serializable
@SerialName("BikePointDescription")
@LLMDescription("Single bike point description")
data class BikePointDescription(
    @property:LLMDescription("Bike point name")
    val name: String = "",
    @property:LLMDescription("Latitude of bike point geolocation")
    val locationLat: Double = 0.0,
    @property:LLMDescription("Longitude of bike point geolocation")
    val locationLng: Double = 0.0,
)