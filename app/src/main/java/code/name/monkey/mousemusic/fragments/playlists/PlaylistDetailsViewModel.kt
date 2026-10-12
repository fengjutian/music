package code.name.monkey.mousemusic.fragments.playlists

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import code.name.monkey.mousemusic.db.PlaylistWithSongs
import code.name.monkey.mousemusic.db.SongEntity
import code.name.monkey.mousemusic.repository.RealRepository

class PlaylistDetailsViewModel(
    private val realRepository: RealRepository,
    private var playlistId: Long
) : ViewModel() {
    fun getSongs(): LiveData<List<SongEntity>> =
        realRepository.playlistSongs(playlistId)

    fun playlistExists(): LiveData<Boolean> =
        realRepository.checkPlaylistExists(playlistId)

    fun getPlaylist(): LiveData<PlaylistWithSongs> = realRepository.getPlaylist(playlistId)
}
