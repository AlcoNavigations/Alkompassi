package fi.metropolia.alkompassi.remote

import com.google.gson.annotations.SerializedName

object AlkoNetworkModel {
    data class Response(val results: List<Results>)
    data class Results(val geometry: Geometry, val name: String, @SerializedName("place_id") val placeID: String)
    data class Geometry(val location: Location)
    data class Location(val lat: Double, val lng: Double)
}