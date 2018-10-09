package fi.metropolia.alkompassi.ui.favorite

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable2
import android.graphics.drawable.Drawable
import android.hardware.SensorManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import fi.metropolia.alkompassi.R
import fi.metropolia.alkompassi.adapters.AlkoListAdapter
import fi.metropolia.alkompassi.ar.ArFragment
import fi.metropolia.alkompassi.data.TempData
import fi.metropolia.alkompassi.data.entities.FavoriteAlko
import fi.metropolia.alkompassi.datamodels.Alko
import fi.metropolia.alkompassi.repositories.LocationRepository
import fi.metropolia.alkompassi.util.DatabaseManager
import fi.metropolia.alkompassi.utils.DatamodelConverters
import fi.metropolia.alkompassi.utils.MapHolder
import kotlinx.android.synthetic.main.maps_fragment.*

class FavoriteFragment: Fragment(), MapHolder {

    private lateinit var viewModel: ViewModel
    private lateinit var v: View

    companion object {
        fun newInstance() = FavoriteFragment()
    }

    private lateinit var locationRepository: LocationRepository
    private lateinit var mapView: MapView
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
    private lateinit var favoriteList: MutableList<FavoriteAlko>
    private lateinit var sensorManager: SensorManager
    private lateinit var dbManager: DatabaseManager

    private var favoriteLiveData: MutableLiveData<List<FavoriteAlko>> = MutableLiveData()
    private var location: Location? = null
    private var locationLoaded: Boolean = false
    private var alkos: MutableList<Alko> = mutableListOf()
    private var mMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        v = inflater.inflate(R.layout.maps_fragment, container, false)
        return v
    }

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dbManager = DatabaseManager(context)
        favoriteLiveData.value = dbManager.doAsyncGetFavorites()
        favoriteList = mutableListOf()
        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
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

        recyclerView = v.findViewById<RecyclerView>(R.id.RecyclerView_nearby_alkolist).apply {
            setHasFixedSize(false)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        locationRepository = LocationRepository.getInstance(activity!!)

        expandAnimatable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
            override fun onAnimationEnd(drawable: Drawable?) {
                super.onAnimationEnd(drawable)
                collapseImageView.visibility = View.VISIBLE
                expandImageView.visibility = View.GONE
            }
        })

        collapseAnimatable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
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

        floatingActionButton.setOnClickListener {
            val fragManager = fragmentManager
            fragManager?.beginTransaction()?.replace(R.id.container, ArFragment())?.commitNow()
        }

        floatingActionButtonDirections.setOnClickListener{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(
                    "http://maps.google.com/maps?saddr=${TempData.myLat},${TempData.myLng}&daddr=${TempData.alkoLat},${TempData.alkoLng}"))
            startActivity(intent)
        }

        locationRepository.getLocation()?.observe(this, Observer<Location> {
            location = it
            if (!locationLoaded && location != null) {
                locationLoaded = true
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
            }

            favoriteLiveData.observe(this, Observer<List<FavoriteAlko>> {
                alkos.clear()
                mMap?.clear()
                favoriteList.clear()
                for (alko in it) {
                    mMap?.addMarker(MarkerOptions().position(LatLng(alko.latitude, alko.longitude)))
                    alkos.add(DatamodelConverters.favoriteAlkoToNetworkAlko(alko))
                    favoriteList.add(alko)
                }
                (viewAdapter as AlkoListAdapter).updateFavorites(favoriteList)
                viewAdapter.notifyDataSetChanged()
            })


            mMap?.setOnMyLocationButtonClickListener {
                Toast.makeText(context, "MyLocation button clicked", Toast.LENGTH_SHORT).show()
                false
            }

            mMap?.setOnMyLocationClickListener {
                Toast.makeText(context, "Current location:\n$it", Toast.LENGTH_LONG).show()
            }

            mMap!!.setOnMarkerClickListener(object : GoogleMap.OnMarkerClickListener {
                override fun onMarkerClick(marker: Marker): Boolean {
                    TempData.alkoLat = marker.position.latitude
                    TempData.alkoLng = marker.position.longitude

                    return false
                }
            })
        }

    }

    fun updateFavorites() {
        favoriteLiveData.value = dbManager.doAsyncGetFavorites()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun getLocation(): Location? {
        return location
    }

}