package fi.metropolia.alkompassi.remote

object Model {
    data class Response(val results: List<Results>)
    data class Results(val geometry: Geometry, val name: String)
    data class Geometry(val location: Location)
    data class Location(val lat: Double, val lng: Double)
}