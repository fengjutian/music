package code.name.monkey.mousemusic.model.smartplaylist

import code.name.monkey.mousemusic.App
import code.name.monkey.mousemusic.R
import code.name.monkey.mousemusic.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class ShuffleAllPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.action_shuffle_all),
    iconRes = R.drawable.ic_shuffle
) {
    override fun songs(): List<Song> {
        return songRepository.songs()
    }
}