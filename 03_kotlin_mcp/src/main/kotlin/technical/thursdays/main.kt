package technical.thursdays

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
import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.putJsonObject
import kotlinx.serialization.json.*

fun main() {

    val server = Server(
        serverInfo = Implementation(
            name = "miejski-bike-server",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(
                    listChanged = true
                )
            )
        )
    )

    // Register a tool to fetch weather alerts by state
    server.addTool(
        name = "get_bike_points",
        description = """
            Provide details like names or addresses for specific bike points from miejski.bike service
        """.trimIndent(),
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("zoneId") {
                    put("type", "string")
                    put("description", "Zone name in Polish, it could be city name like poznan or some area like trojmiasto")
                }
            },
            required = listOf("zoneId")
        )
    ) { request ->
        val zoneId = request.arguments["zoneId"]?.jsonPrimitive?.content ?: return@addTool CallToolResult(
            content = listOf(TextContent("The 'zoneId' parameter is required."))
        )

        val result = execute(zoneId = zoneId, "rack")
        val sb = StringBuilder()

        result.list.take(10).forEach {
            sb.append(it)
        }

        CallToolResult(content = listOf(TextContent(sb.toString().trimIndent())) )
    }

    // Create a transport using standard IO for server communication
    val transport = StdioServerTransport(
        System.`in`.asInput(),
        System.out.asSink().buffered()
    )

    runBlocking {
        server.connect(transport)
        val done = Job()
        server.onClose {
            done.complete()
        }
        done.join()
    }
}

suspend fun execute(zoneId: String, typeId: String): BikeResult {
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
            headers.append(HttpHeaders.Authorization, "SzdEYlY8Nlc1TGJwVEoqcVhLPHMzVj1CNXo8L2ZmUWovPCxlaHtxUmBFc1s5JyYrO2tlLWtidUErPic2P3tbemYydlF7KEo2dUFYTV9hOjMnVXNlJyxIfVp9KTQ+bUxLM3Mhdz4mWDY6XXU7YihYe3N5SyokcCpZJ0c6TWNDaHFAKCo1eywyR2EjczV1a1tGOmF7IWtwTFpiU3ByWzs5akQtezRZRXFDc3k9VSxCPlEsdWUzUmJKP2h1M3M4PXZleSE0QiVlK1tbPFpWfWV9WHg2dzdoTUM9RyZgU2pDSFFVQ2JRQl9FRF9eO21MZyV6Y2orUjt+WnlYe0dgLnoveQ==")
            port = 8444
            url {
                appendPathSegments("api", "v1", "zones", zoneId.lowercase(), "points")
            }
        }
    )

    try {
        val typeId = "${typeId}_${zoneId}".lowercase()
//        println("execute ----------> args zone: $zoneId type: $typeId request: " + httpResponse.request.url)

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

//        println("execute ----------> zoneId: " + zoneId + " size: " + response.size)

//        response.forEach {
//            println("point: $it")
//        }

        return BikeResult(response)
    } catch (error: Exception) {
//        println("execute ----------> error: $error")
        return BikeResult(emptyList())
    }
}