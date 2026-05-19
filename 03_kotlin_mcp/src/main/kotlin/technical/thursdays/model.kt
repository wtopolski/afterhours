package technical.thursdays

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

@Serializable
data class BikeResult(val list: List<BikePoint>)