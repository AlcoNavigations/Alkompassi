package fi.metropolia.alkompassi.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoriteAlko")
data class FavoriteAlko(@PrimaryKey(autoGenerate = false) val placeID: String,
                        val name: String,
                        val latitude: Double,
                        val longitude: Double)