package code.name.monkey.mousemusic.model.smartplaylist

import code.name.monkey.mousemusic.App
import code.name.monkey.mousemusic.R
import code.name.monkey.mousemusic.model.Song
import kotlinx.parcelize.Parcelize

@Parcelize
class LastAddedPlaylist : AbsSmartPlaylist(
    name = App.getContext().getString(R.string.last_added),
    iconRes = R.drawable.ic_library_add
) {
    override fun songs(): List<Song> {
        return lastAddedRepository.recentSongs()
    }
}