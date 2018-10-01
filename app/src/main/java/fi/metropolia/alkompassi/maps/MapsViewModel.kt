package fi.metropolia.alkompassi.maps

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import fi.metropolia.alkompassi.remote.IGoogleAPIService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import androidx.lifecycle.MutableLiveData
import fi.metropolia.alkompassi.datamodels.Alko
import io.reactivex.disposables.CompositeDisposable

class MapsViewModel : ViewModel() {

    private var nearAlkos: MutableLiveData<Alko>? = MutableLiveData()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var alkoDist: Double = 0.0

    fun getNearAlkos(): MutableLiveData<Alko>? {
        return nearAlkos
    }

    private val wikiApiServe by lazy {
        IGoogleAPIService.create()
    }

    fun beginSearch(location: Location) {

        val searchablePlace = "alko"
        val radius = "5000"
        val apiKey = "AIzaSyDr5EFKYZWL2E33Bvi46bPEEg0pOqS0rq4"


        //Linter warnings are here because this IDE won't show parameter names if the parameter values aren't given in string format
        //Do not erase!
        val disposable =
                wikiApiServe.getAlkoAddresses(
                        "${location.latitude},${location.longitude}",
                        "$radius",
                        "alkoholi",
                        "$searchablePlace",
                        "$apiKey")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                { result ->
                                    for (alko in result.results) {
                                        Log.d("Alko found: ", "${alko.geometry.location.lat}" + "${alko.geometry.location.lng}")
                                        nearAlkos?.value = Alko(alko.name, alko.geometry.location.lat,alko.geometry.location.lng)

                                        alkoDist = distFrom(location.latitude, location.longitude, alko.geometry.location.lat, alko.geometry.location.lng)
                                    }
                                }
                                ,
                                { error -> Log.d("Error: ",error.message) }
                        )
        if (disposable != null) compositeDisposable.add(disposable)
    }

    fun distFrom(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val earthRadius = 6371000.0 //meters
        val dLat = Math.toRadians((lat2 - lat1))
        val dLng = Math.toRadians((lng2 - lng1))
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2) * Math.sin(dLng / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return (earthRadius * c)
    }

    fun distToAlko(alko: Alko, location: Location) : Double{
        return distFrom(location.latitude, location.longitude, alko.lat, alko.lng)
    }
}