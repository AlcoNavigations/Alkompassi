package fi.metropolia.alkompassi.maps

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import fi.metropolia.alkompassi.remote.IGoogleAPIService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import androidx.lifecycle.MutableLiveData
import fi.metropolia.alkompassi.data.TempData
import io.reactivex.disposables.CompositeDisposable

class MapsViewModel : ViewModel() {

    private var nearAlkos: MutableLiveData<LatLng>? = MutableLiveData()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var degrees: Double = 0.0

    fun getNearAlkos(): MutableLiveData<LatLng>? {
        return nearAlkos
    }

    private val wikiApiServe by lazy {
        IGoogleAPIService.create()
    }

    fun beginSearch(location: Location) {

        val searchablePlace = "alko"
        val radius = "500"
        val apiKey = "AIzaSyDr5EFKYZWL2E33Bvi46bPEEg0pOqS0rq4"


        //Linter warnings are here because this IDE won't show parameter names if the parameter values aren't given in a string format
        //Do not "fix" these!
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
                                        nearAlkos?.value = LatLng(alko.geometry.location.lat,alko.geometry.location.lng)

                                        TempData.alkoLat = alko.geometry.location.lat
                                        TempData.alkoLng = alko.geometry.location.lng

                                        refreshDegrees(location.latitude, location.longitude)
                                    }
                                }
                                ,
                                { error -> Log.d("Error: ",error.message) }
                        )
        if (disposable != null) compositeDisposable.add(disposable)
    }

    fun refreshDegrees(currLat: Double, currLon: Double){
        degrees = getDegreesToAlko(currLat, currLon, TempData.alkoLat, TempData.alkoLng)
        TempData.alkoDegrees = degrees
    }

    fun getDegreesToAlko(lat1: Double, lon1: Double, lat2: Double, lon2: Double) : Double{
        //1 = start location. 2 = end location

        val dLon = (lon2-lon1)
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(dLon)
        var brng = Math.toDegrees((Math.atan2(y, x)))
        brng = (360 - ((brng + 360) % 360))

        return brng
    }


}