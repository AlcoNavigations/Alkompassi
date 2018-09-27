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
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import fi.metropolia.alkompassi.R

class MapsFragment : Fragment(), LocationListener {

    private val dialogRequest = 9001

    companion object {
        fun newInstance() = MapsFragment()
    }

    private lateinit var viewModel: MapsViewModel
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null
    private lateinit var v: View

    private var myLat: Double = 0.0
    private var myLon: Double = 0.0

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
            val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

            if (googleMap != null) mMap = googleMap
            // Add a marker in Sydney and move the camera
            val myLoc = LatLng(location.latitude, location.longitude)
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 16F))

            mMap?.isMyLocationEnabled = true
            mMap?.setOnMyLocationButtonClickListener {
                Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
                false
            }

            mMap?.setOnMyLocationClickListener {
                Toast.makeText(context, "Current location:\n$it", Toast.LENGTH_LONG).show()
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


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }


    override fun onLocationChanged(p0: Location?) {
        Log.d("GEOLOCATION", "new latitude: $myLat and longitude: $myLon altitude: ${p0?.altitude} ")
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(p0: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
