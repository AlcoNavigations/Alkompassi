package fi.metropolia.alkompassi.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favoriteLocation")
data class FavoriteLocation(@PrimaryKey(autoGenerate = false) val PlaceID : String,
                            val name : String)