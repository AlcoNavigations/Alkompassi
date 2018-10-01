package fi.metropolia.alkompassi.ar

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import com.google.ar.sceneform.ux.ArFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.dynamic.SupportFragmentWrapper
import fi.metropolia.alkompassi.R
import kotlinx.android.synthetic.main.maps_activity.*


class ArFragment : Fragment() /*LocationListener*/ {

    private lateinit var fragment : ArFragment
    private lateinit var ar: View

    companion object {
        fun newInstance() = ArFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        ar = inflater.inflate(R.layout.ar_fragment, container, false)

        Log.d("AAAAAAAAAAAAAAAAAAA","dsfdsf")
        //fragment = supportFragmentManager!!.findFragmentById(R.id.sceneform_fragment) as ArFragment

        return ar
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


    }

    /*
    override fun onLocationChanged(location: Location?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderEnabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onProviderDisabled(provider: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }*/


}