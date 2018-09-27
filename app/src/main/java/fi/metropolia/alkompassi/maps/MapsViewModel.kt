package fi.metropolia.alkompassi.maps

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import fi.metropolia.alkompassi.remote.IGoogleAPIService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import androidx.lifecycle.MutableLiveData
import io.reactivex.disposables.CompositeDisposable


class MapsViewModel : ViewModel() {

    private var nearAlkos: MutableLiveData<LatLng>? = MutableLiveData()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

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
        var address : Address

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
                                { result ->
                                    for (alko in result.candidates) {
                                        Log.d("Osoite: ", alko.formatted_address)
                                        address = coder.getFromLocationName(alko.formatted_address,1)[0]
                                        val alkoLat = address.latitude
                                        val alkoLon = address.longitude
                                        nearAlkos?.value = LatLng(alkoLat,alkoLon)
                                    }

                                }
                                ,
                                { error -> Log.d("Error: ",error.message) }
                        )
        if (disposable != null) compositeDisposable.add(disposable)
    }



}