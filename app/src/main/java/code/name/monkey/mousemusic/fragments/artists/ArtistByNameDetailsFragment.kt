package code.name.monkey.mousemusic.fragments.artists

import androidx.navigation.fragment.navArgs
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Shows the songs and albums of a track artist addressed by name instead of by id.
 *
 * Used when the artist of the current song shares the credit, e.g. "Artist1 / Artist2" or
 * "Artist1 feat. Artist2", because a single artist of such a credit has no own artist id.
 */
class ArtistByNameDetailsFragment : AbsArtistDetailsFragment() {

    private val arguments by navArgs<ArtistByNameDetailsFragmentArgs>()

    override val detailsViewModel: ArtistDetailsViewModel by viewModel {
        parametersOf(null, arguments.extraArtistName, false)
    }
    override val artistId: Long?
        get() = null
    override val artistName: String
        get() = arguments.extraArtistName
}
