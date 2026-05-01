package org.example.miejskibike

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolArgs
import ai.koog.agents.core.tools.ToolDescriptor
import ai.koog.agents.core.tools.ToolParameterDescriptor
import ai.koog.agents.core.tools.ToolParameterType
import ai.koog.agents.core.tools.ToolResult.JSONSerializable
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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