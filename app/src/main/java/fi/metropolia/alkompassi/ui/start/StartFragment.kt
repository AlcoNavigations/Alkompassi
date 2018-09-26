package fi.metropolia.alkompassi.ui.start

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import fi.metropolia.alkompassi.R
import android.content.Intent
import kotlinx.android.synthetic.main.start_fragment.*


class StartFragment : Fragment() {

    lateinit var v : View

    companion object {
        fun newInstance() = StartFragment()
    }

    private lateinit var viewModel: StartViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        v  = inflater.inflate(R.layout.start_fragment, container, false)
        return v
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(StartViewModel::class.java)

        getMapsButton.setOnClickListener{
            startMapsActivity(v)
        }
    }

    fun startMapsActivity(view: View) {
        val intent = Intent(view.context, MapsActivity::class.java)
        startActivity(intent)
    }
}
