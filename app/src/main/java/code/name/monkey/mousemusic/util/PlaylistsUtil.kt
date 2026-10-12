package code.name.monkey.mousemusic.util

import code.name.monkey.mousemusic.db.PlaylistWithSongs
import code.name.monkey.mousemusic.helper.M3UWriter.writeIO
import java.io.File
import java.io.IOException

object PlaylistsUtil {
    @Throws(IOException::class)
    fun savePlaylistWithSongs(playlist: PlaylistWithSongs?): File {
        return writeIO(
            File(getExternalStorageDirectory(), "Playlists"), playlist!!
        )
    }
}