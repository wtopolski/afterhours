@file:Suppress("MISSING_DEPENDENCY_SUPERCLASS_IN_TYPE_ARGUMENT", "MISSING_DEPENDENCY_SUPERCLASS_WARNING")

package pl.wtopolski.koog.samples

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.annotation.InternalAgentsApi
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.ReceivedToolResults
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls
import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.dsl.prompt
import ai.koog.serialization.JSONPrimitive
import ai.koog.serialization.typeToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.port
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val lmStudioExecutor = lmStudioExecutor()

val mbAgentStrategy = strategy<String, Any>("Bike points Location Assistant of miejski.bike service") {
    val nodeSendInput by nodeLLMRequest()
    val nodeExecuteTool by nodeExecuteTools()
    @OptIn(InternalAgentsApi::class)
    val nodeParseResult by node<ReceivedToolResults, ResponseDescription> { toolResults ->
        val toolResult = toolResults.toolResults.firstOrNull() ?: return@node ResponseDescription()
        val zoneId = (toolResult.toolArgs.entries["zoneId"] as? JSONPrimitive)?.content ?: ""
        val typeId = (toolResult.toolArgs.entries["typeId"] as? JSONPrimitive)?.content ?: ""
        val bikeResult = toolResult.resultObject as? BikeResult
        ResponseDescription(
            bikeType = typeId,
            zoneName = zoneId,
            numberOfPoint = bikeResult?.content?.size ?: 0,
            bikePoints = bikeResult?.content?.map { BikePointDescription(it.name, it.locationLat, it.locationLng) } ?: emptyList()
        )
    }

    edge(nodeStart forwardTo nodeSendInput)
    edge((nodeSendInput forwardTo nodeFinish) onTextMessage { true })
    edge((nodeSendInput forwardTo nodeExecuteTool) onToolCalls { true })
    edge(nodeExecuteTool forwardTo nodeParseResult)
    edge(nodeParseResult forwardTo nodeFinish)
}

suspend fun main() {
    val agentConfig = AIAgentConfig(
        prompt = prompt("miejski-bike") {
            system(
                """
                You are a Bike Points Location Assistant.

                You provide details for specific bike points locations. When the user requests a location or city, extract its ID.
                The response includes all fetched data about the points.

                Use only data from registered tools. Call the tool immediately without any explanation or preamble.
                /no_think
                """.trimIndent()
            )
        },
        model = lm_studio_qwen3_5,
        maxAgentIterations = 10
    )

    val toolRegistry = ToolRegistry {
        tools(listOf(SayToUser, MiejskiBikeTool))
    }

    val agent = AIAgent(
        promptExecutor = lmStudioExecutor,
        strategy = mbAgentStrategy,
        agentConfig = agentConfig,
        toolRegistry = toolRegistry
    )

    printLogo()

    while (true) {
        print("You: ")
        val input = readLine() ?: break
        if (input == "/exit") break
        val result = agent.run(input)
        if (result is ResponseDescription) printBikePoints(result)
        else println(result)
    }
}

private fun printLogo() {
    println("""
    __  ____        _      __   _    ____  _ __           ________          __
   /  |/  (_)__    (_)____/ /__(_)  / __ )(_) /_____     / ____/ /_  ____ _/ /_
  / /|_/ / / _ \  / / ___/ //_/ /  / __  / / //_/ _ \   / /   / __ \/ __ `/ __/
 / /  / / /  __/ / (__  ) ,< / /  / /_/ / / ,< /  __/  / /___/ / / / /_/ / /_
/_/  /_/_/\___/_/ /____/_/|_/_/  /_____/_/_/|_|\___/   \____/_/ /_/\__,_/\__/
             /___/
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
 Ask about bike racks, workshops, and wrench stations in Lodz, Warszawa, Poznan.
 Type /exit to quit.
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    """.trimIndent())
}

private fun printBikePoints(result: ResponseDescription) {
    val title = "${result.bikeType.uppercase()} in ${result.zoneName.uppercase()} — ${result.numberOfPoint} point(s)"

    if (result.bikePoints.isEmpty()) {
        println("\n$title\nNo points found.\n")
        return
    }

    val rows = result.bikePoints.mapIndexed { i, p ->
        listOf("${i + 1}", p.name, "%.6f".format(p.locationLat), "%.6f".format(p.locationLng))
    }
    val headers = listOf("#", "Name", "Latitude", "Longitude")
    val cols = headers.indices.map { col ->
        maxOf(headers[col].length, rows.maxOf { it[col].length })
    }

    fun row(cells: List<String>, left: String, mid: String, right: String) =
        left + cells.mapIndexed { i, c -> " ${c.padEnd(cols[i])} " }.joinToString(mid) + right

    fun divider(left: String, mid: String, right: String, fill: String) =
        left + cols.joinToString(mid) { fill.repeat(it + 2) } + right

    println()
    println(title)
    println(divider("+", "+", "+", "-"))
    println(row(headers, "|", "|", "|"))
    println(divider("+", "+", "+", "-"))
    rows.forEach { println(row(it, "|", "|", "|")) }
    println(divider("+", "+", "+", "-"))
    println()
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
    private val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    override suspend fun execute(args: BikeRequestArgs): BikeResult {
        println("\n  > [miejski.bike] type=${args.typeId}  zone=${args.zoneId}")
        val httpResponse: HttpResponse = httpClient.get(
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

        return try {
            val typeId = "${args.typeId}_${args.zoneId}".lowercase()
            val response = httpResponse.body<List<DTOBikePoint>>()
                .filter { bikePoint -> bikePoint.typeId == typeId }
                .take(10)
                .map { point ->
                    val name = point.attrs.firstOrNull() { it.key == "name" }?.value
                    val address = point.attrs.firstOrNull() { it.key == "address" }?.value
                    BikePoint(name = name ?: address ?: "no name", locationLat = point.location.lat, locationLng = point.location.lng)
                }
            println("  > [miejski.bike] ${response.size} result(s) found")
            BikeResult(response)
        } catch (error: Exception) {
            println("  > [miejski.bike] error: $error")
            BikeResult(emptyList())
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

val bikePointsExamples = listOf(
    ResponseDescription(
        bikeType = "workshop",
        zoneName = "warszawa",
        numberOfPoint = 1,
        bikePoints = listOf(
            BikePointDescription(name = "Mój rower", locationLat = 50.02, locationLng = 22.8)
        )
    ),
    ResponseDescription(
        bikeType = "wrench",
        zoneName = "lodz",
        numberOfPoint = 2,
        bikePoints = listOf(
            BikePointDescription(name = "Mój rower", locationLat = 50.02, locationLng = 22.8),
            BikePointDescription(name = "Mój rower 2", locationLat = 40.02, locationLng = 28.8)
        )
    ),
    ResponseDescription(bikeType = "rack", zoneName = "poznan", numberOfPoint = 0)
)

@Serializable
@SerialName("ResponseDescription")
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
