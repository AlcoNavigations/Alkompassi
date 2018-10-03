package fi.metropolia.alkompassi.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import fi.metropolia.alkompassi.utils.SingletonHolder

class LocationRepository private constructor(activity: FragmentActivity?) : LocationListener {

    var lm: LocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    init {
        requestLocationUpdates()
    }

    companion object : SingletonHolder<LocationRepository, FragmentActivity>(::LocationRepository)

    private var location: MutableLiveData<Location>? = MutableLiveData()

    fun getLocation(): MutableLiveData<Location>? {
        return location
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f, this)
    }

    override fun onLocationChanged(p0: Location?) {
        Log.d("GEOLOCATION", "new latitude: ${p0?.latitude} and longitude: ${p0?.longitude} altitude: ${p0?.altitude} ")
        location?.value = p0
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }


}