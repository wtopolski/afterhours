package org.example.miejskibike

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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