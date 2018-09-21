package fi.metropolia.alkompassi.ui.end

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import fi.metropolia.alkompassi.R

class EndFragment : Fragment() {

    companion object {
        fun newInstance() = EndFragment()
    }

    private lateinit var viewModel: EndViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Set up a click listener on the login button
        view?.findViewById<Button>(R.id.toStartButton)?.setOnClickListener {
            // Navigate to the login destination
            view?.let { Navigation.findNavController(it).navigate(R.id.end) }
        }


        return inflater.inflate(R.layout.end_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(EndViewModel::class.java)
        // TODO: Use the ViewModel
    }

}