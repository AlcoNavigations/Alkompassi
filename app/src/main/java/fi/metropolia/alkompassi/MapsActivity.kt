package fi.metropolia.alkompassi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import fi.metropolia.alkompassi.maps.MapsFragment


class MapsActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps_activity)


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MapsFragment.newInstance())
                    .commitNow()
        }
    }

}
