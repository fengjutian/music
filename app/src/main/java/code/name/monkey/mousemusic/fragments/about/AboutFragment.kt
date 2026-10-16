package code.name.monkey.mousemusic.fragments.about

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import code.name.monkey.mousemusic.R
import code.name.monkey.mousemusic.databinding.FragmentAboutBinding
import code.name.monkey.mousemusic.util.NavigationUtil
import dev.chrisbanes.insetter.applyInsetter

class AboutFragment : Fragment(R.layout.fragment_about) {
    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAboutBinding.bind(view)
        binding.aboutContent.cardOther.openSource.setOnClickListener {
            NavigationUtil.goToOpenSource(requireActivity())
        }

        binding.aboutContent.root.applyInsetter {
            type(navigationBars = true) {
                padding(vertical = true)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
