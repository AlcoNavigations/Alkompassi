package fi.metropolia.alkompassi.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fi.metropolia.alkompassi.data.dataobjects.FavoriteLocationDao
import fi.metropolia.alkompassi.data.entities.FavoriteAlko

@Database(entities = [FavoriteAlko::class], version = 1)
abstract class ApplicationDatabase : RoomDatabase() {
    abstract fun favoriteLocationDao(): FavoriteLocationDao

    companion object {
        private var INSTANCE: ApplicationDatabase? = null

        fun getInstance(context: Context): ApplicationDatabase? {
            if (INSTANCE == null) {
                synchronized(ApplicationDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            ApplicationDatabase::class.java, "weather.db")
                            .build()
                }
            }
            return INSTANCE
        }

    }
}