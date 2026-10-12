package code.name.monkey.mousemusic.interfaces

import code.name.monkey.mousemusic.model.Album
import code.name.monkey.mousemusic.model.Artist
import code.name.monkey.mousemusic.model.Genre

interface IHomeClickListener {
    fun onAlbumClick(album: Album)

    fun onArtistClick(artist: Artist)

    fun onGenreClick(genre: Genre)
}