package fi.metropolia.alkompassi.maps

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import fi.metropolia.alkompassi.remote.IGoogleAPIService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import androidx.lifecycle.MutableLiveData
import fi.metropolia.alkompassi.datamodels.Alko
import io.reactivex.disposables.CompositeDisposable

class MapsViewModel : ViewModel() {

    private var nearAlkos: MutableLiveData<Alko>? = MutableLiveData()
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

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
                                        Log.d("Alko found: ", "${alko.geometry.location.lat} ${alko.geometry.location.lng} ${alko.placeID}")
                                        nearAlkos?.value = Alko(alko.name, alko.geometry.location.lat,alko.geometry.location.lng, alko.placeID)
                                    }
                                }
                                ,
                                { error -> Log.d("getAlkoError: ",error.message) }
                        )
        if (disposable != null) compositeDisposable.add(disposable)
    }

}