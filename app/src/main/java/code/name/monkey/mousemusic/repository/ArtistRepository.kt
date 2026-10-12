package code.name.monkey.mousemusic.repository

import android.provider.MediaStore.Audio.AudioColumns
import code.name.monkey.mousemusic.ALBUM_ARTIST
import code.name.monkey.mousemusic.helper.SortOrder
import code.name.monkey.mousemusic.model.Album
import code.name.monkey.mousemusic.model.Artist
import code.name.monkey.mousemusic.util.ArtistSeparator
import code.name.monkey.mousemusic.util.PreferenceUtil
import java.text.Collator

interface ArtistRepository {
    fun artists(): List<Artist>

    fun artists(albums: List<Album>, albumArtistsOnly: Boolean): List<Artist>

    fun albumArtists(): List<Artist>

    fun albumArtists(query: String): List<Artist>

    fun artists(query: String): List<Artist>

    fun artist(artistId: Long): Artist

    fun albumArtist(artistName: String): Artist

    fun artist(artistName: String): Artist
}

class RealArtistRepository(
    private val songRepository: RealSongRepository,
    private val albumRepository: RealAlbumRepository
) : ArtistRepository {

    private fun getSongLoaderSortOrder(): String {
        return PreferenceUtil.artistSortOrder + ", " +
                PreferenceUtil.artistAlbumSortOrder + ", " +
                PreferenceUtil.artistSongSortOrder
    }

    override fun artist(artistId: Long): Artist {
        if (artistId == Artist.VARIOUS_ARTISTS_ID) {
            // Get Various Artists
            val songs = songRepository.songs(
                songRepository.makeSongCursor(
                    null,
                    null,
                    getSongLoaderSortOrder()
                )
            )
            val albums = albumRepository.splitIntoAlbums(songs)
                .filter { it.albumArtist == Artist.VARIOUS_ARTISTS_DISPLAY_NAME }
            return Artist(Artist.VARIOUS_ARTISTS_ID, albums)
        }

        val songs = songRepository.songs(
            songRepository.makeSongCursor(
                AudioColumns.ARTIST_ID + "=?",
                arrayOf(artistId.toString()),
                getSongLoaderSortOrder()
            )
        )
        return Artist(artistId, albumRepository.splitIntoAlbums(songs))
    }

    override fun albumArtist(artistName: String): Artist {
        if (artistName == Artist.VARIOUS_ARTISTS_DISPLAY_NAME) {
            // Get Various Artists
            val songs = songRepository.songs(
                songRepository.makeSongCursor(
                    null,
                    null,
                    getSongLoaderSortOrder()
                )
            )
            val albums = albumRepository.splitIntoAlbums(songs)
                .filter { it.albumArtist == Artist.VARIOUS_ARTISTS_DISPLAY_NAME }
            return Artist(Artist.VARIOUS_ARTISTS_ID, albums, true)
        }

        val songs = songRepository.songs(
            songRepository.makeSongCursor(
                "album_artist" + "=?",
                arrayOf(artistName),
                getSongLoaderSortOrder()
            )
        )
        val albums = albumRepository.splitIntoAlbums(songs)
        if (albums.isEmpty()) {
            return Artist.empty
        }
        return Artist(artistName, albums, true)
    }

    /**
     * Looks up an artist by its track artist name, including songs where the artist shares the
     * credit, e.g. `"Artist1 / Artist2"` or `"Artist1 feat. Artist2"`.
     *
     * Only the songs credited to [artistName] are collected so that the albums of the returned
     * artist don't contain songs of the other artists of a shared credit.
     */
    override fun artist(artistName: String): Artist {
        val songs = songRepository.songs(
            songRepository.makeSongCursor(
                AudioColumns.ARTIST + " LIKE ?",
                arrayOf("%$artistName%"),
                getSongLoaderSortOrder()
            )
        ).filter { ArtistSeparator.contains(it.artistName, artistName) }
        if (songs.isEmpty()) {
            return Artist.empty
        }
        return Artist(artistName, albumRepository.splitIntoAlbums(songs))
    }

    override fun artists(): List<Artist> {
        val songs = songRepository.songs(
            songRepository.makeSongCursor(
                null, null,
                getSongLoaderSortOrder()
            )
        )
        val artists = splitIntoArtists(albumRepository.splitIntoAlbums(songs))
        return sortArtists(artists)
    }

    override fun artists(albums: List<Album>, albumArtistsOnly: Boolean): List<Artist> {
        val artists = if (albumArtistsOnly) {
            splitIntoAlbumArtists(albums)
        } else {
            splitIntoArtists(albums)
        }
        return sortArtists(artists)
    }

    override fun albumArtists(): List<Artist> {
        val songs = songRepository.songs(
            songRepository.makeSongCursor(
                null,
                null,
                "lower($ALBUM_ARTIST)" +
                        if (PreferenceUtil.artistSortOrder == SortOrder.ArtistSortOrder.ARTIST_A_Z) "" else " DESC"
            )
        )
        val artists = splitIntoAlbumArtists(albumRepository.splitIntoAlbums(songs))
        return sortArtists(artists)
    }

    override fun albumArtists(query: String): List<Artist> {
        val songs = songRepository.songs(
            songRepository.makeSongCursor(
                "album_artist" + " LIKE ?",
                arrayOf("%$query%"),
                getSongLoaderSortOrder()
            )
        )
        val artists = splitIntoAlbumArtists(albumRepository.splitIntoAlbums(songs))
        return sortArtists(artists)
    }

    override fun artists(query: String): List<Artist> {
        val songs = songRepository.songs(
            songRepository.makeSongCursor(
                AudioColumns.ARTIST + " LIKE ?",
                arrayOf("%$query%"),
                getSongLoaderSortOrder()
            )
        )
        val artists = splitIntoArtists(albumRepository.splitIntoAlbums(songs))
        return sortArtists(artists)
    }


    private fun splitIntoAlbumArtists(albums: List<Album>): List<Artist> {
        return albums.groupBy { it.albumArtist }
            .filter {
                !it.key.isNullOrEmpty()
            }
            .map {
                val currentAlbums = it.value
                if (currentAlbums.isNotEmpty()) {
                    if (currentAlbums[0].albumArtist == Artist.VARIOUS_ARTISTS_DISPLAY_NAME) {
                        Artist(Artist.VARIOUS_ARTISTS_ID, currentAlbums, true)
                    } else {
                        Artist(currentAlbums[0].artistId, currentAlbums, true)
                    }
                } else {
                    Artist.empty
                }
            }
    }


    fun splitIntoArtists(albums: List<Album>): List<Artist> {
        return albums.groupBy { it.artistId }
            .map { Artist(it.key, it.value) }
    }

    private fun sortArtists(artists: List<Artist>): List<Artist> {
        val collator = Collator.getInstance()
        return when (PreferenceUtil.artistSortOrder) {
            SortOrder.ArtistSortOrder.ARTIST_A_Z -> {
                artists.sortedWith { a1, a2 -> collator.compare(a1.name, a2.name) }
            }
            SortOrder.ArtistSortOrder.ARTIST_Z_A -> {
                artists.sortedWith { a1, a2 -> collator.compare(a2.name, a1.name) }
            }
            else -> artists
        }
    }
}
