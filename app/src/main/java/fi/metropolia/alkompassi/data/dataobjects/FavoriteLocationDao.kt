package fi.metropolia.alkompassi.data.dataobjects

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import fi.metropolia.alkompassi.data.entities.FavoriteAlko

@Dao
interface FavoriteLocationDao {

    @Query("SELECT * from favoritealko")
    fun getAll() : List<FavoriteAlko>

    @Insert(onConflict = REPLACE)
    fun insert(favoriteAlko: FavoriteAlko)

    @Query("DELETE from favoritealko")
    fun deleteAll()

    @Delete
    fun delete(model: FavoriteAlko)

}