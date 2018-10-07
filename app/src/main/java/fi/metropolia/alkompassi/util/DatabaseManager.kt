package fi.metropolia.alkompassi.util

import android.content.Context
import fi.metropolia.alkompassi.data.ApplicationDatabase
import fi.metropolia.alkompassi.data.entities.FavoriteAlko
import fi.metropolia.alkompassi.datamodels.Alko
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

class DatabaseManager(context: Context?){

    private val mDb = ApplicationDatabase.getInstance(context!!)

    fun doAsyncSaveFavorites(dataset: List<Alko>, position: Int): Unit? {
        fun doAsync(dataset: List<Alko>, position: Int) : Deferred<Unit?> {
            return async {
                mDb?.favoriteLocationDao()?.insert(FavoriteAlko(dataset[position].placeID, dataset[position].name, dataset[position].lat, dataset[position].lng))
            }
        }
        return runBlocking {
            doAsync(dataset, position).await()
        }

    }

    fun doAsyncDeleteFavorites(dataset: List<Alko>, position: Int, favoriteAlkos: List<FavoriteAlko>) {
        fun doAsync(dataset: List<Alko>, position: Int): Deferred<Unit> {
            return async {
                if(favoriteAlkos.find { it.PlaceID == dataset[position].placeID} != null) {
                    mDb?.favoriteLocationDao()?.delete(favoriteAlkos.find { it.PlaceID == dataset[position].placeID }!!)
                }
            }
        }
        return runBlocking {
            doAsync(dataset, position).await()
        }
    }

    fun doAsyncGetFavorites(): List<FavoriteAlko> {
        fun doAsync(): Deferred<List<FavoriteAlko>> {
            return async {
                mDb?.favoriteLocationDao()?.getAll() ?: listOf()
            }
        }
        return runBlocking {
            doAsync().await()
        }
    }
}