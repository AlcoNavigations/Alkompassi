package fi.metropolia.alkompassi.utils

import fi.metropolia.alkompassi.data.entities.FavoriteAlko
import fi.metropolia.alkompassi.datamodels.Alko

abstract class DatamodelConverters {
    companion object {
        fun networkAlkoToFavoriteAlko(alko: Alko): FavoriteAlko {
            return FavoriteAlko(
                    alko.placeID,
                    alko.name,
                    alko.lat,
                    alko.lng)
        }

        fun favoriteAlkoToNetworkAlko(alko: FavoriteAlko): Alko {
            return Alko(
                    alko.name,
                    alko.latitude,
                    alko.longitude,
                    alko.placeID
            )
        }
    }
}