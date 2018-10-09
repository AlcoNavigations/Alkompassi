package fi.metropolia.alkompassi.repositories

import android.annotation.SuppressLint
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import fi.metropolia.alkompassi.data.TempData
import fi.metropolia.alkompassi.utils.SingletonHolder

class LocationRepository private constructor(activity: FragmentActivity?) : LocationListener, SensorEventListener {

    var lm: LocationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private var currentDegree = 0f
    private var mSensorManager: SensorManager? = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager


    init {
        requestLocationUpdates()
        requestAzimuthUpdates()
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

    private fun requestAzimuthUpdates() {

        mSensorManager!!.registerListener(this, mSensorManager!!.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onLocationChanged(p0: Location?) {
        Log.d("GEOLOCATION", "new latitude: ${p0?.latitude} and longitude: ${p0?.longitude} altitude: ${p0?.altitude} ")
        location?.value = p0
        TempData.myLat = p0!!.latitude
        TempData.myLng = p0.longitude
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onSensorChanged(event: SensorEvent?) {

        val azimuth = Math.round(event!!.values[0]).toFloat()
        currentDegree = -azimuth
        TempData.rotationDegrees = azimuth.toInt()
    }
}