package technical.thursdays

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.port
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.streams.asInput
import io.modelcontextprotocol.kotlin.sdk.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.Implementation
import io.modelcontextprotocol.kotlin.sdk.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.TextContent
import io.modelcontextprotocol.kotlin.sdk.Tool
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.buffered
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject

private const val API_BASE_URL = "https://storage.miejski.bike"
private const val API_PORT = 8444
private val API_TOKEN: String = System.getenv("MIEJSKI_BIKE_API_TOKEN")
    ?: run {
        System.err.println("Error: MIEJSKI_BIKE_API_TOKEN environment variable is not set.")
        kotlin.system.exitProcess(1)
    }
private const val POINT_TYPE = "rack"
private const val MAX_RESULTS = 10

private val jsonClient = Json { ignoreUnknownKeys = true }
private val jsonOutput = Json { prettyPrint = false }

/** Starts the miejski-bike MCP server over stdio and blocks until the client disconnects. */
fun main() {
    val server = Server(
        serverInfo = Implementation(name = "miejski-bike-server", version = "1.0.0"),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true)
            )
        )
    )

    server.addTool(
        name = "get_bike_points",
        description = "Returns up to $MAX_RESULTS bike rack points (name + GPS) for a given zone from miejski.bike.",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("zoneId") {
                    put("type", "string")
                    put(
                        "description",
                        "Zone name in Polish, e.g. 'poznan', 'warszawa', 'wroclaw'."
                    )
                }
            },
            required = listOf("zoneId")
        )
    ) { request ->
        val zoneId = request.requireString("zoneId")
            ?: return@addTool errorResult("The 'zoneId' parameter is required.")

        jsonResult(fetchBikePoints(zoneId))
    }

    server.addTool(
        name = "get_zones",
        description = "Returns all available miejski.bike zones (city/area ids and display names).",
        inputSchema = Tool.Input(properties = buildJsonObject {}, required = emptyList())
    ) { _ ->
        jsonResult(fetchZones())
    }

    server.addTool(
        name = "get_zone_types",
        description = "Returns the POI types available within a given zone (e.g. rack, wrench, workshop).",
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                putJsonObject("zoneId") {
                    put("type", "string")
                    put("description", "Zone id, e.g. 'poznan'. Use get_zones to discover valid ids.")
                }
            },
            required = listOf("zoneId")
        )
    ) { request ->
        val zoneId = request.requireString("zoneId")
            ?: return@addTool errorResult("The 'zoneId' parameter is required.")

        jsonResult(fetchZoneTypes(zoneId))
    }

    val transport = StdioServerTransport(System.`in`.asInput(), System.out.asSink().buffered())

    runBlocking {
        server.connect(transport)
        val done = Job()
        server.onClose { done.complete() }
        done.join()
    }
}

/**
 * Fetches up to [MAX_RESULTS] bike rack points for the given zone.
 * Returns an empty list on any HTTP or parsing failure.
 */
suspend fun fetchBikePoints(zoneId: String): List<BikePoint> {
    val normalizedZone = zoneId.lowercase()
    val typeFilter = "${POINT_TYPE}_$normalizedZone"

    val raw = fetchFromApi<List<DTOBikePoint>>("api", "v1", "zones", normalizedZone, "points")
        ?: return emptyList()

    return raw
        .filter { it.typeId == typeFilter }
        .take(MAX_RESULTS)
        .map { point ->
            BikePoint(
                name = point.attrs.findName() ?: point.attrs.findAddress() ?: "no name",
                locationLat = point.location.lat,
                locationLng = point.location.lng
            )
        }
}

/** Fetches all known miejski.bike zones with their display names. */
suspend fun fetchZones(): List<BikeZone> {
    val raw = fetchFromApi<List<DTOZone>>("api", "v1", "zones") ?: return emptyList()
    return raw.map { BikeZone(id = it.id, name = it.attrs.findName() ?: it.id) }
}

/** Fetches the POI types (rack, wrench, workshop, …) available in the given zone. */
suspend fun fetchZoneTypes(zoneId: String): List<BikeZoneType> {
    val normalizedZone = zoneId.lowercase()
    val raw = fetchFromApi<List<DTOPoiType>>("api", "v1", "zones", normalizedZone, "types")
        ?: return emptyList()
    return raw.map {
        BikeZoneType(id = it.id, zoneId = it.zoneId, name = it.attrs.findName() ?: it.id)
    }
}

private suspend inline fun <reified T> fetchFromApi(vararg pathSegments: String): T? =
    HttpClient(CIO) {
        install(ContentNegotiation) { json(jsonClient) }
    }.use { client ->
        try {
            client.get(API_BASE_URL) {
                applyDefaultHeaders()
                port = API_PORT
                url { appendPathSegments(*pathSegments) }
            }.body<T>()
        } catch (_: Exception) {
            null
        }
    }

private fun HttpRequestBuilder.applyDefaultHeaders() {
    headers.append(HttpHeaders.ContentType, "application/json")
    headers.append(HttpHeaders.UserAgent, "miejski-bike-mcp/1.0.0")
    headers.append(HttpHeaders.AcceptLanguage, "PL-pl")
    headers.append(HttpHeaders.Authorization, API_TOKEN)
}

private fun List<Attr>.findName(): String? = firstOrNull { it.key == "name" }?.value
private fun List<Attr>.findAddress(): String? = firstOrNull { it.key == "address" }?.value

private fun io.modelcontextprotocol.kotlin.sdk.CallToolRequest.requireString(key: String): String? =
    arguments[key]?.jsonPrimitive?.content

private inline fun <reified T> jsonResult(value: T): CallToolResult =
    CallToolResult(content = listOf(TextContent(jsonOutput.encodeToString(value))))

private fun errorResult(message: String): CallToolResult =
    CallToolResult(content = listOf(TextContent(message)))

@Serializable
data class BikePoint(
    val name: String,
    val locationLat: Double,
    val locationLng: Double
)

@Serializable
data class BikeZone(
    val id: String,
    val name: String
)

@Serializable
data class BikeZoneType(
    val id: String,
    val zoneId: String,
    val name: String
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
data class DTOZone(
    val id: String,
    val attrs: List<Attr>
)

@Serializable
data class DTOPoiType(
    val id: String,
    val zoneId: String,
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
    val tag: Boolean? = null,
    val warning: Boolean? = null
)
