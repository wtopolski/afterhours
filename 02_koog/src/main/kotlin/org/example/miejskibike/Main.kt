@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package org.example.miejskibike

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
import ai.koog.agents.core.tools.ToolResult.JSONSerializable
import ai.koog.agents.ext.tool.SayToUser
import ai.koog.prompt.dsl.Prompt
import ai.koog.prompt.executor.llms.all.simpleOllamaAIExecutor
import ai.koog.prompt.message.Message
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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
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

object MiejskiBikeTool : Tool<BikeRequestArgs, BikeResult>() {

    override val argsSerializer = BikeRequestArgs.serializer()

    override val descriptor = ToolDescriptor(
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
                headers.append(HttpHeaders.UserAgent, "PostmanRuntime/7.44.1")
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
) : ToolArgs

@Serializable
data class BikeResult(
    val content: List<BikePoint>
) : JSONSerializable<BikeResult> {
    override fun getSerializer(): KSerializer<BikeResult> = serializer()
}

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

// Generate JSON Schema
val bikePointsStructure = JsonStructuredData.createJsonStructure<ResponseDescription>(
    schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
    examples = exampleForecasts,
    schemaType = JsonStructuredData.JsonSchemaType.SIMPLE
)

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