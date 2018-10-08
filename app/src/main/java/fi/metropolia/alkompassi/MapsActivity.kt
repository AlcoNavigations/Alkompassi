package fi.metropolia.alkompassi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment.findNavController
import fi.metropolia.alkompassi.maps.MapsFragment
import kotlinx.android.synthetic.main.maps_activity.*


class MapsActivity : AppCompatActivity(){



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps_activity)


        //val navController = Navigation.findNavController(this.my_nav_host_fragment.view!!)
        //navController.navigate(R.id.mapsFragment)




        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MapsFragment.newInstance())
                    .commitNow()
        }
    }


}
