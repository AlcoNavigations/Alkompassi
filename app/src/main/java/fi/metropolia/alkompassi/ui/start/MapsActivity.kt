package fi.metropolia.alkompassi.ui.start

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import fi.metropolia.alkompassi.R
import kotlinx.android.synthetic.main.activity_maps.*



class MapsActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var mMap: GoogleMap

    var myLat: Double = 0.0
    var myLon: Double = 0.0

    var prevLat: Double = 0.0
    var prevLon: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if ((Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),0)
        }

        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

        refreshButton.setOnClickListener{
            refreshLocation(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER))
        }
    }

    private fun refreshLocation(p0: Location?){
        if (p0 != null) {
            prevLat = myLat
            prevLon = myLon
            myLat = p0.latitude
            myLon = p0.longitude
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(LatLng(myLat,myLon)))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((LatLng(myLat,myLon)),15f))

            //distFrom(myLat.toFloat(), myLon.toFloat(), prevLat.toFloat(), prevLon.toFloat())
            //textView.text = "Your latitude is: ${myLat}, longitude: ${myLon} and altitude: ${p0?.altitude}. prevlat:${prevLat} prevlon:${prevLon} Distance since last check is ${distFrom(myLat.toFloat(), myLon.toFloat(), prevLat.toFloat(), prevLon.toFloat())} meters."
        }
    }

    fun distFrom(lat1: Float, lng1: Float, lat2: Float, lng2: Float): Float {
        val earthRadius = 6371000.0 //meters
        val dLat = Math.toRadians((lat2 - lat1).toDouble())
        val dLng = Math.toRadians((lng2 - lng1).toDouble())
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1.toDouble())) * Math.cos(Math.toRadians(lat2.toDouble())) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return (earthRadius * c).toFloat()
    }

    override fun onLocationChanged(p0: Location?) {

        Log.d("GEOLOCATION", "new latitude: ${myLat} and longitude: ${myLon} altitude: ${p0?.altitude} ")
        6 }
    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?)
    {}
    override fun onProviderEnabled(p0: String?) {}
    override fun onProviderDisabled(p0: String?) {}

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val myLoc = LatLng(myLat, myLon)
        mMap.addMarker(MarkerOptions().position(myLoc).title("Marker in MyLocation"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLoc))
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }
}
