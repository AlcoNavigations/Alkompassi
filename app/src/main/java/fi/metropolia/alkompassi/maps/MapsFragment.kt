package fi.metropolia.alkompassi.maps

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import fi.metropolia.alkompassi.R
import fi.metropolia.alkompassi.Remote.IGoogleAPIService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.maps_fragment.*

class MapsFragment : Fragment(), LocationListener {

    val ERROR_DIALOG_REQUEST = 9001

    val wikiApiServe by lazy {
        IGoogleAPIService.create()
    }
    var disposable: Disposable? = null


    companion object {
        fun newInstance() = MapsFragment()
    }

    private lateinit var viewModel: MapsViewModel
    private lateinit var mapView: MapView
    private var mMap: GoogleMap? = null
    lateinit var v: View

    var myLat: Double = 0.0
    var myLon: Double = 0.0

    var prevLat: Double = 0.0
    var prevLon: Double = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        v = inflater.inflate(R.layout.maps_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = v.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {

            if (it != null) mMap = it
            // Add a marker in Sydney and move the camera
            val myLoc = LatLng(myLat, myLon)
            mMap?.addMarker(MarkerOptions().position(myLoc).title("Marker in MyLocation"))
            mMap?.moveCamera(CameraUpdateFactory.newLatLng(myLoc))

            if (( ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(activity as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)
            }

            val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

            refreshButton.setOnClickListener {
                refreshLocation(lm.getLastKnownLocation(LocationManager.GPS_PROVIDER))
                beginSearch()
            }
        }

        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)

        if (availability == ConnectionResult.SUCCESS) {
            Log.d("DBG", "GOOGLE SERVICE AVAILABLE")
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(availability)) {
            Log.d("DBG", "FIXABLE ERROR")
            val dialog: Dialog = GoogleApiAvailability.getInstance().getErrorDialog(activity, availability, ERROR_DIALOG_REQUEST)
        }

    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MapsViewModel::class.java)

    }

    private fun beginSearch() {

        val searchablePlace : String
        val radius: String
        val apiKey : String

        val coder = Geocoder(context)
        var address : List<Address>


        searchablePlace = "alko"
        radius = "2000"
        apiKey = "AIzaSyDr5EFKYZWL2E33Bvi46bPEEg0pOqS0rq4"

        disposable =
                wikiApiServe.getAlkoAddresses(
                        "$searchablePlace",
                        "textquery",
                        "photos,formatted_address,name,opening_hours,rating",
                        "circle:$radius@$myLat,$myLon",
                        "$apiKey")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result -> Log.d("Osoite: ", result.candidates[0].formatted_address)
                                    address = coder.getFromLocationName(result.candidates[0].formatted_address,1)
                                    var alkoLat = address.get(0).latitude
                                    var alkoLon = address.get(0).longitude
                                    var alkoLoc = LatLng(alkoLat,alkoLon)
                                    mMap?.addMarker(MarkerOptions().position(alkoLoc))
                                }

                                ,
                                { error -> Log.d("Error: ",error.message) }
                        )
    }


    private fun refreshLocation(p0: Location?) {
        if (p0 != null) {
            Log.d("LATLON:", p0.toString())
            prevLat = myLat
            prevLon = myLon
            myLat = p0.latitude
            myLon = p0.longitude
            mMap?.clear()
            mMap?.addMarker(MarkerOptions().position(LatLng(myLat, myLon)))
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom((LatLng(myLat, myLon)), 15f))

            //distFrom(myLat.toFloat(), myLon.toFloat(), prevLat.toFloat(), prevLon.toFloat())
            //textView.text = "Your latitude is: ${myLat}, longitude: ${myLon} and altitude: ${p0?.altitude}. prevlat:${prevLat} prevlon:${prevLon} Distance since last check is ${distFrom(myLat.toFloat(), myLon.toFloat(), prevLat.toFloat(), prevLon.toFloat())} meters."
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }


    override fun onLocationChanged(p0: Location?) {
        Log.d("GEOLOCATION", "new latitude: ${myLat} and longitude: ${myLon} altitude: ${p0?.altitude} ")
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
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }

    }
}
