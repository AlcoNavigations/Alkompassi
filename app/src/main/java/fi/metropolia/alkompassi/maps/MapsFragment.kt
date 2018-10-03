package fi.metropolia.alkompassi.maps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Animatable2.AnimationCallback
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import fi.metropolia.alkompassi.R
import fi.metropolia.alkompassi.adapters.AlkoListAdapter
import fi.metropolia.alkompassi.datamodels.Alko
import fi.metropolia.alkompassi.repositories.LocationRepository
import fi.metropolia.alkompassi.utils.MapHolder

class MapsFragment : Fragment(), MapHolder {
    override fun getLocation(): Location? {
        return location
    }

    private val dialogRequest = 9001

    companion object {
        fun newInstance() = MapsFragment()
    }

    private lateinit var viewModel: MapsViewModel
    private lateinit var locationRepository: LocationRepository
    private lateinit var mapView: MapView
    private lateinit var v: View
    private lateinit var expandImageView: ImageView
    private lateinit var collapseImageView: ImageView
    private lateinit var expandAnimatable: Animatable2
    private lateinit var collapseAnimatable: Animatable2
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var bottomSheet: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var bottomSheetHeader: View

    private var location: Location? = null
    private var locationLoaded: Boolean = false
    private var alkos: MutableList<Alko> = mutableListOf()
    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(activity as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        v = inflater.inflate(R.layout.maps_fragment, container, false)
        return v
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        expandImageView = v.findViewById(R.id.imageView_expand_animatable)
        collapseImageView = v.findViewById(R.id.imageView_collapse_animatable)
        expandAnimatable = expandImageView.drawable as Animatable2
        collapseAnimatable = collapseImageView.drawable as Animatable2
        bottomSheet = v.findViewById(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetHeader = v.findViewById(R.id.bottom_sheet_header)
        viewManager = LinearLayoutManager(context)
        viewAdapter = AlkoListAdapter(alkos, this)

        locationRepository = LocationRepository.getInstance(activity!!)

        recyclerView = v.findViewById<RecyclerView>(R.id.RecyclerView_nearby_alkolist).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        expandAnimatable.registerAnimationCallback(object : AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                super.onAnimationEnd(drawable)
                collapseImageView.visibility = View.VISIBLE
                expandImageView.visibility = View.GONE
            }
        })

        collapseAnimatable.registerAnimationCallback(object : AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                super.onAnimationEnd(drawable)
                collapseImageView.visibility = View.GONE
                expandImageView.visibility = View.VISIBLE
            }
        })

        bottomSheetHeader.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                collapseAnimatable.start()
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                expandAnimatable.start()
            }
        }

        locationRepository.getLocation()?.observe(this, Observer<Location> {
            location = it
            if (!locationLoaded && location != null) {
                locationLoaded = true
                viewModel.beginSearch(location!!)
            }
        })

        mapView = v.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { googleMap ->

            if (googleMap != null) mMap = googleMap
            mMap?.isMyLocationEnabled = true

            mMap?.uiSettings?.isMapToolbarEnabled = false

            if (location != null) {
                val myLoc = LatLng(location!!.latitude, location!!.longitude)
                mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 16F))
                viewModel.beginSearch(location!!)
            }

            // Listen for the Alko locations
            viewModel.getNearAlkos()?.observe(this, Observer<Alko> {
                mMap?.addMarker(MarkerOptions().position(LatLng(it.lat, it.lng)))
                alkos.add(it)
                viewAdapter.notifyDataSetChanged()
            })

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
