package fi.metropolia.alkompassi.utils

import android.location.Location

interface MapHolder {
    fun getLocation() : Location?
}