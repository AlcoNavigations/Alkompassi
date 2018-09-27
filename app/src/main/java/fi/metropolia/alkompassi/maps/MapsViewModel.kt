package fi.metropolia.alkompassi.maps

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import fi.metropolia.alkompassi.Remote.IGoogleAPIService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import androidx.lifecycle.MutableLiveData



class MapsViewModel : ViewModel() {

    private var nearAlkos: MutableLiveData<LatLng>? = MutableLiveData()

    fun getNearAlkos(): MutableLiveData<LatLng>? {
        return nearAlkos
    }

    private val wikiApiServe by lazy {
        IGoogleAPIService.create()
    }

    fun beginSearch(context: Context, location: Location) {

        val searchablePlace = "alko"
        val radius = "2000"
        val apiKey = "AIzaSyDr5EFKYZWL2E33Bvi46bPEEg0pOqS0rq4"

        val coder = Geocoder(context)
        var address : List<Address>

        val disposable =
                wikiApiServe.getAlkoAddresses(
                        searchablePlace,
                        "textquery",
                        "photos,formatted_address,name,opening_hours,rating",
                        "circle:$radius@${location.latitude},${location.longitude}",
                        apiKey)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result -> Log.d("Osoite: ", result.candidates[0].formatted_address)
                                    address = coder.getFromLocationName(result.candidates[0].formatted_address,1)
                                    val alkoLat = address[0].latitude
                                    val alkoLon = address[0].longitude
                                    nearAlkos?.value = LatLng(alkoLat,alkoLon)
                                }
                                ,
                                { error -> Log.d("Error: ",error.message) }
                        )
    }

}