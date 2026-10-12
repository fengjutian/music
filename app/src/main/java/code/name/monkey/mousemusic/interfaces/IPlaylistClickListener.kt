package code.name.monkey.mousemusic.interfaces

import android.view.View
import code.name.monkey.mousemusic.db.PlaylistWithSongs

interface IPlaylistClickListener {
    fun onPlaylistClick(playlistWithSongs: PlaylistWithSongs, view: View)
}