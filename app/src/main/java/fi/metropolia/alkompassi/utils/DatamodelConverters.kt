package fi.metropolia.alkompassi.utils

import fi.metropolia.alkompassi.data.entities.FavoriteAlko
import fi.metropolia.alkompassi.datamodels.Alko

abstract class DatamodelConverters {
    companion object {
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