package ci.nsu.moble.main.ui.main

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import ci.nsu.moble.main.MainActivity

import ci.nsu.moble.main.R

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val activity = requireActivity() as MainActivity

        return ComposeView(requireContext()).apply {
            setContent {
                activity.UserTheme(activity.colorsMap)
            }
        }
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}