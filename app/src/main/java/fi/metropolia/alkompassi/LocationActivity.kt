package fi.metropolia.alkompassi

import android.app.Activity
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log

class LocationActivity  : Activity(), LocationListener {


    override fun onLocationChanged(p0: Location?) {

        Log.d("GEOLOCATION", "new latitude: ${p0?.latitude} and longitude: ${p0?.longitude}")
        6 }
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?)
    {}
    override fun onProviderEnabled(p0: String?) {}
    override fun onProviderDisabled(p0: String?) {}

}