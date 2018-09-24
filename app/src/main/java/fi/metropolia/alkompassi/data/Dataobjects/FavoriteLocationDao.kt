package fi.metropolia.alkompassi.data.Dataobjects

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import fi.metropolia.alkompassi.data.entities.FavoriteLocation

@Dao
interface FavoriteLocationDao {

    @Query("SELECT * from favoritelocation")
    fun getAll() : List<FavoriteLocation>

    @Insert(onConflict = REPLACE)
    fun insert(favoriteLocation: FavoriteLocation)

    @Query("DELETE from favoriteLocation")
    fun deleteAll()



}