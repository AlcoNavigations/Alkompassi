package fi.metropolia.alkompassi.ui.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

class FavoriteFragment: Fragment() {

    private lateinit var viewModel: ViewModel

    companion object {
        fun newInstance() = FavoriteFragment()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FavoriteViewModel::class.java)
    }

}