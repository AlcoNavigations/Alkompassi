package fi.metropolia.alkompassi.maps

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import fi.metropolia.alkompassi.R
import fi.metropolia.alkompassi.datamodels.Alko

class MapsFragment : Fragment(), LocationListener {

    private val dialogRequest = 9001

    companion object {
        fun newInstance() = MapsFragment()
    }

    private lateinit var viewModel: MapsViewModel
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null
    private lateinit var v: View
    var alkos : MutableList<Alko> = mutableListOf()

    private var location : Location? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        v = inflater.inflate(R.layout.maps_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = v.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { googleMap ->

            if (( ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(activity as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)
            }

            val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            location = updateLocation()
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)


            if (googleMap != null) mMap = googleMap
            val myLoc = LatLng(location!!.latitude, location!!.longitude)
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 16F))
            mMap?.isMyLocationEnabled = true

            viewModel.beginSearch(location!!)
            
            // Listen for the Alko locations
            viewModel.getNearAlkos()?.observe(this, Observer<Alko> {
                mMap?.addMarker(MarkerOptions().position(LatLng(it.lat, it.lng)))
                alkos.add(it)
            })

            mMap?.setOnMyLocationButtonClickListener {
                Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
                false
            }

            mMap?.setOnMyLocationClickListener {
                Toast.makeText(context, "Current location:\n$it", Toast.LENGTH_LONG).show()
            }

            mMap!!.setOnMarkerClickListener {

                val alkoDist = alkos.find { alko -> alko.lat == it.position.latitude && alko.lng == it.position.longitude }

                Toast.makeText(context, alkoDist!!.name.toString() + "\nDistance: " + "%.2f".format(viewModel.distToAlko(alkoDist!!, location!!)) + " meters", Toast.LENGTH_LONG).show()
                true
            }

        }

        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)

        if (availability == ConnectionResult.SUCCESS) {
            Log.d("DBG", "GOOGLE SERVICE AVAILABLE")
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(availability)) {
            Log.d("DBG", "FIXABLE ERROR")
            GoogleApiAvailability.getInstance().getErrorDialog(activity, availability, dialogRequest)
        }

    }

    fun updateLocation() : Location? {
        if (( ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(activity as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }

        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers : List<String> = lm.getProviders(true)
        var bestLocation: Location? = null

        for (provider in providers) {
            val location = lm.getLastKnownLocation(provider) ?: continue
            if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                bestLocation = location
            }
        }
        return bestLocation
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }


    override fun onLocationChanged(p0: Location?) {
        Log.d("GEOLOCATION", "new latitude: ${p0?.latitude} and longitude: ${p0?.longitude} altitude: ${p0?.altitude} ")
        location = p0
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }

    }
}
