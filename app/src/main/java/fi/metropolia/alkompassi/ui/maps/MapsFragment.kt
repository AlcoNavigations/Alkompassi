package fi.metropolia.alkompassi.ui.maps

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Animatable2.AnimationCallback
import android.graphics.drawable.Drawable
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
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
import com.google.android.gms.maps.model.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.seismic.ShakeDetector
import fi.metropolia.alkompassi.R
import fi.metropolia.alkompassi.data.TempData
import fi.metropolia.alkompassi.adapters.AlkoListAdapter
import fi.metropolia.alkompassi.ui.ar.ArFragment
import fi.metropolia.alkompassi.data.entities.FavoriteAlko
import fi.metropolia.alkompassi.datamodels.Alko
import fi.metropolia.alkompassi.repositories.LocationRepository
import fi.metropolia.alkompassi.utils.DatabaseManager
import fi.metropolia.alkompassi.utils.MapHolder
import kotlinx.android.synthetic.main.maps_fragment.*
import kotlinx.android.synthetic.main.maps_fragment.view.*

class MapsFragment : Fragment(), MapHolder, ShakeDetector.Listener {
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
    private lateinit var favoriteList: List<FavoriteAlko>
    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var vibrator: Vibrator
    private lateinit var dbManager: DatabaseManager

    private var location: Location? = null
    private var locationLoaded: Boolean = false
    private var alkos: MutableList<Alko> = mutableListOf()
    private var mMap: GoogleMap? = null
    private var fetchingAlkoLocations = false

    override fun hearShake() {
        Toast.makeText(context, "Refreshing", Toast.LENGTH_SHORT).show()
        if (location != null && !fetchingAlkoLocations) {
            fetchingAlkoLocations = true
            viewModel.beginSearch(location!!)
            zoomToLocation()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                //deprecated in API 26 -- Lint warning suppressed
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }

    override fun getLocation(): Location? {
        return location
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(activity as Activity, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }
        locationRepository = LocationRepository.getInstance(activity!!)
        locationRepository.getLocation()?.observe(this, Observer<Location> {
            location = it
            if (!locationLoaded && location != null) {
                locationLoaded = true
                viewModel.beginSearch(location!!)
                zoomToLocation()
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        v = inflater.inflate(R.layout.maps_fragment, container, false)
        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dbManager = DatabaseManager(context)
        favoriteList = dbManager.doAsyncGetFavorites()
        sensorManager = activity?.getSystemService(SENSOR_SERVICE) as SensorManager
        shakeDetector = ShakeDetector(this)
        shakeDetector.start(sensorManager)
        expandImageView = v.findViewById(R.id.imageView_expand_animatable)
        collapseImageView = v.findViewById(R.id.imageView_collapse_animatable)
        expandAnimatable = expandImageView.drawable as Animatable2
        collapseAnimatable = collapseImageView.drawable as Animatable2
        bottomSheet = v.findViewById(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetHeader = v.findViewById(R.id.bottom_sheet_header)
        viewManager = LinearLayoutManager(context)
        viewAdapter = AlkoListAdapter(alkos, this, context, favoriteList)
        floatingActionButton.hide()
        floatingActionButtonDirections.hide()
        vibrator = activity?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        setupBottomsheet()
        setupFabs()
        setupMap(savedInstanceState)
        checkGoogleApiAvailability()

    }

    private fun checkGoogleApiAvailability() {
        val availability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)

        if (availability == ConnectionResult.SUCCESS) {
            Log.d("DBG", "GOOGLE SERVICE AVAILABLE")
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(availability)) {
            Log.d("DBG", "FIXABLE ERROR")
            GoogleApiAvailability.getInstance().getErrorDialog(activity, availability, dialogRequest)
        }
    }

    fun zoomToLocation(){
        val myLoc = LatLng(location!!.latitude, location!!.longitude)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(myLoc, 10.5F))
    }

    @SuppressLint("MissingPermission")
    private fun setupMap(savedInstanceState: Bundle?) {
        mapView = v.findViewById(R.id.map)
        mapView.onCreate(savedInstanceState)

        mapView.getMapAsync { googleMap ->

            if (googleMap != null) mMap = googleMap
            mMap?.isMyLocationEnabled = true
            mMap?.uiSettings?.isMapToolbarEnabled = false
            mMap?.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.style_json))

            // Listen for the Alko locations
            viewModel.getNearAlkos()?.observe(this, Observer<List<Alko>> { alkolist ->
                alkos.clear()
                mMap?.clear()
                for (alko in alkolist) {
                    if (favoriteList.find { it.placeID == alko.placeID } != null) {
                        mMap?.addMarker(MarkerOptions().position(LatLng(alko.lat, alko.lng)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)))
                    } else {
                        mMap?.addMarker(MarkerOptions().position(LatLng(alko.lat, alko.lng)))
                    }
                    alkos.add(alko)
                }
                viewAdapter.notifyDataSetChanged()
                fetchingAlkoLocations = false
            })

            mMap?.setOnMapClickListener {
                v.floatingActionButton!!.hide()
                v.floatingActionButtonDirections!!.hide()
            }

            mMap!!.setOnMarkerClickListener { marker ->
                TempData.alkoLat = marker.position.latitude
                TempData.alkoLng = marker.position.longitude
                Log.d("Marker: ", marker.position.latitude.toString() + " " + marker.position.longitude.toString())
                Log.d("Temp: ", TempData.alkoLat.toString() + " " + TempData.alkoLng.toString())
                v.floatingActionButton!!.show()
                v.floatingActionButtonDirections!!.show()
                false
            }
        }
    }

    private fun setupFabs() {
        floatingActionButton.setOnClickListener {
            val fragManager = fragmentManager
            val fragmentTransaction = fragManager?.beginTransaction()
            fragmentTransaction?.replace(R.id.container, ArFragment())
            fragmentTransaction?.addToBackStack(null)?.commit()
        }

        floatingActionButtonDirections.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                    "http://maps.google.com/maps?saddr=${TempData.myLat},${TempData.myLng}&daddr=${TempData.alkoLat},${TempData.alkoLng}"))
            startActivity(intent)
        }
    }

    private fun setupBottomsheet() {
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
                floatingActionButton.hide()
                floatingActionButtonDirections.hide()
            } else {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                expandAnimatable.start()
            }
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

}
