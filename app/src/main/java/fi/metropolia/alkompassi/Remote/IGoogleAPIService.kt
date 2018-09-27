package fi.metropolia.alkompassi.Remote

import fi.metropolia.alkompassi.maps.MapsFragment
import io.reactivex.Observable
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface IGoogleAPIService {

    @GET ("api/place/findplacefromtext/json?")
    fun getAlkoAddresses(@Query("input") input: String,
                         @Query("inputtype") inputtype: String,
                         @Query("fields") fields: String,
                         @Query("locationbias") locationbias: String,
                         @Query("key") key: String):
            Observable<Model.Result>


    companion object {
        fun create(): IGoogleAPIService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl("https://maps.googleapis.com/maps/")
                    .build()

            return retrofit.create(IGoogleAPIService::class.java)
        }
    }

}