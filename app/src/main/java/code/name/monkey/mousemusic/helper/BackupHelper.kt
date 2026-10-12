package code.name.monkey.mousemusic.helper

import android.content.Context
import android.os.Environment
import code.name.monkey.mousemusic.BuildConfig
import code.name.monkey.mousemusic.R
import code.name.monkey.mousemusic.db.PlaylistEntity
import code.name.monkey.mousemusic.db.toSongEntity
import code.name.monkey.mousemusic.extensions.showToast
import code.name.monkey.mousemusic.extensions.zipOutputStream
import code.name.monkey.mousemusic.helper.BackupContent.*
import code.name.monkey.mousemusic.model.Song
import code.name.monkey.mousemusic.repository.Repository
import code.name.monkey.mousemusic.repository.SongRepository
import code.name.monkey.mousemusic.util.getExternalStoragePublicDirectory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

object BackupHelper : KoinComponent {
    private val repository by inject<Repository>()
    private val songRepository by inject<SongRepository>()

    suspend fun createBackup(context: Context, name: String) {
        val backupFile =
            File(getBackupRoot(), name + APPEND_EXTENSION)
        if (backupFile.parentFile?.exists() != true) {
            backupFile.parentFile?.mkdirs()
        }
        val zipItems = mutableListOf<ZipItem>()
        zipItems.addAll(getPlaylistZipItems(context))
        zipItems.addAll(getSettingsZipItems(context))
        getUserImageZipItems(context)?.let { zipItems.addAll(it) }
        zipItems.addAll(getCustomArtistZipItems(context))
        zipAll(context, zipItems, backupFile)
        // Clean Cache Playlist Directory
        File(context.filesDir, PLAYLISTS_PATH).deleteRecursively()
    }

    private suspend fun zipAll(context: Context, zipItems: List<ZipItem>, backupFile: File) =
        withContext(Dispatchers.IO) {
            runCatching {
                backupFile.outputStream().buffered().zipOutputStream().use { out ->
                    for (zipItem in zipItems) {
                        File(zipItem.filePath).inputStream().buffered().use { origin ->
                            val entry = ZipEntry(zipItem.zipPath)
                            out.putNextEntry(entry)
                            origin.copyTo(out)
                        }
                    }
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    context.showToast(R.string.error_create_backup)
                }
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    context.showToast(R.string.message_backup_create_success)
                }
            }
        }

    private suspend fun getPlaylistZipItems(context: Context): List<ZipItem> {
        val playlistZipItems = mutableListOf<ZipItem>()
        // Cache Playlist files in App storage
        val playlistFolder = File(context.filesDir, PLAYLISTS_PATH)
        if (!playlistFolder.exists()) {
            playlistFolder.mkdirs()
        }
        for (playlist in repository.fetchPlaylistWithSongs()) {
            runCatching {
                M3UWriter.writeIO(playlistFolder, playlist)
            }.onSuccess { playlistFile ->
                if (playlistFile.exists()) {
                    playlistZipItems.add(
                        ZipItem(
                            playlistFile.absolutePath,
                            PLAYLISTS_PATH.child(playlistFile.name)
                        )
                    )
                }
            }
        }
        return playlistZipItems
    }

    private fun getSettingsZipItems(context: Context): List<ZipItem> {
        val sharedPrefPath = File(context.filesDir.parentFile, "shared_prefs")
        return listOf(
            "${BuildConfig.APPLICATION_ID}_preferences.xml", // App settings pref path
            "$THEME_PREFS_KEY_DEFAULT.xml"  // appthemehelper pref path
        ).map {
            ZipItem(File(sharedPrefPath, it).absolutePath, SETTINGS_PATH.child(it))
        }
    }

    private fun getUserImageZipItems(context: Context): List<ZipItem>? {
        return context.filesDir.listFiles { _, name ->
            name.endsWith(".jpg")
        }?.map {
            ZipItem(it.absolutePath, IMAGES_PATH.child(it.name))
        }
    }

    private fun getCustomArtistZipItems(context: Context): List<ZipItem> {
        val zipItemList = mutableListOf<ZipItem>()
        val sharedPrefPath = File(context.filesDir.parentFile, "shared_prefs")

        zipItemList.addAll(
            File(context.filesDir, "custom_artist_images")
                .listFiles()?.map {
                    ZipItem(
                        it.absolutePath,
                        CUSTOM_ARTISTS_PATH.child("custom_artist_images").child(it.name)
                    )
                }?.toList() ?: listOf()
        )
        File(sharedPrefPath, "custom_artist_image.xml").let {
            if (it.exists()) {
                zipItemList.add(
                    ZipItem(
                        it.absolutePath,
                        CUSTOM_ARTISTS_PATH.child("prefs").child("custom_artist_image.xml")
                    )
                )
            }
        }
        return zipItemList
    }

    suspend fun restoreBackup(
        context: Context,
        inputStream: InputStream?,
        contents: List<BackupContent>
    ) {
        withContext(Dispatchers.IO) {
            ZipInputStream(inputStream).use {
                var entryCount = 0
                var restoredBytes = 0L
                var entry = it.nextEntry
                while (entry != null) {
                    entryCount++
                    if (entryCount > MAX_BACKUP_ENTRIES) {
                        throw IOException("Backup contains too many entries")
                    }
                    if (entry.isPlaylistEntry() && contents.contains(PLAYLISTS)) {
                        val data = it.readCheckedEntry(restoredBytes)
                        restoredBytes += data.size
                        restorePlaylists(data, entry)
                    } else if (entry.isPreferenceEntry() && contents.contains(SETTINGS)) {
                        val data = it.readCheckedEntry(restoredBytes)
                        restoredBytes += data.size
                        restorePreferences(context, data, entry)
                    } else if (entry.isImageEntry() && contents.contains(USER_IMAGES)) {
                        val data = it.readCheckedEntry(restoredBytes)
                        restoredBytes += data.size
                        restoreImages(context, data, entry)
                    } else if (entry.isCustomArtistEntry() && contents.contains(CUSTOM_ARTIST_IMAGES)) {
                        val data = it.readCheckedEntry(restoredBytes)
                        restoredBytes += data.size
                        if (entry.isCustomArtistPrefEntry()) {
                            restoreCustomArtistPrefs(context, data, entry)
                        } else if (entry.isCustomArtistImageEntry()) {
                            restoreCustomArtistImages(context, data, entry)
                        }
                    }
                    entry = it.nextEntry
                }
            }
            withContext(Dispatchers.Main) {
                context.showToast(R.string.message_restore_success)
            }
        }
    }

    private fun restoreImages(context: Context, data: ByteArray, zipEntry: ZipEntry) {
        val file = File(
            context.filesDir.path, zipEntry.getFileName()
        )
        file.outputStream().buffered().use { bos ->
            data.inputStream().copyTo(bos)
        }
    }

    private fun restorePreferences(context: Context, data: ByteArray, zipEntry: ZipEntry) {
        val file = File(
            context.filesDir.parent!! + File.separator + "shared_prefs" + File.separator + zipEntry.getFileName()
        )
        if (file.exists()) {
            file.delete()
        }
        file.outputStream().buffered().use { bos ->
            data.inputStream().copyTo(bos)
        }
    }

    private suspend fun restorePlaylists(
        data: ByteArray,
        zipEntry: ZipEntry
    ) {
        val playlistName = zipEntry.getFileName().substringBeforeLast(".")
        val songs = mutableListOf<Song>()

        // Get songs from m3u playlist files
        data.inputStream().bufferedReader().useLines { lines ->
            lines.forEach { line ->
                if (line.startsWith(File.separator)) {
                    if (File(line).exists()) {
                        songs.addAll(songRepository.songsByFilePath(line))
                    }
                }
            }
        }
        val playlistEntity = repository.checkPlaylistExists(playlistName).firstOrNull()
        if (playlistEntity != null) {
            val songEntities = songs.map {
                it.toSongEntity(playlistEntity.playListId)
            }
            repository.insertSongs(songEntities)
        } else {
            val playListId = repository.createPlaylist(PlaylistEntity(playlistName = playlistName))
            val songEntities = songs.map {
                it.toSongEntity(playListId)
            }
            repository.insertSongs(songEntities)
        }
    }

    private fun restoreCustomArtistImages(
        context: Context,
        data: ByteArray,
        zipEntry: ZipEntry
    ) {
        val parentFolder = File(context.filesDir, "custom_artist_images")

        if (!parentFolder.exists()) {
            parentFolder.mkdirs()
        }
        val file = File(
            parentFolder,
            zipEntry.getFileName()
        )
        file.outputStream().buffered()
            .use { bos ->
                data.inputStream().copyTo(bos)
            }
    }

    private fun restoreCustomArtistPrefs(
        context: Context,
        data: ByteArray,
        zipEntry: ZipEntry
    ) {
        val file =
            File(context.filesDir.parentFile, "shared_prefs".child(zipEntry.getFileName()))
        file.outputStream().buffered().use { bos ->
            data.inputStream().copyTo(bos)
        }
    }

    fun getBackupRoot(): File {
        return File(
            getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "RetroMusic/Backups"
        )
    }

    const val BACKUP_EXTENSION = "rmbak"
    const val APPEND_EXTENSION = ".$BACKUP_EXTENSION"
    private const val PLAYLISTS_PATH = "Playlists"
    private const val SETTINGS_PATH = "prefs"
    private const val IMAGES_PATH = "userImages"
    private const val CUSTOM_ARTISTS_PATH = "artistImages"
    private const val THEME_PREFS_KEY_DEFAULT = "[[kabouzeid_app-theme-helper]]"

    private fun ZipEntry.isPlaylistEntry(): Boolean {
        return name.startsWith(PLAYLISTS_PATH)
    }

    private fun ZipEntry.isPreferenceEntry(): Boolean {
        return name.startsWith(SETTINGS_PATH) && getFileName() in allowedPreferenceFiles
    }

    private fun ZipEntry.isImageEntry(): Boolean {
        return name.startsWith(IMAGES_PATH)
    }

    private fun ZipEntry.isCustomArtistEntry(): Boolean {
        return name.startsWith(CUSTOM_ARTISTS_PATH)
    }

    private fun ZipEntry.isCustomArtistImageEntry(): Boolean {
        return name.startsWith(CUSTOM_ARTISTS_PATH) && name.contains("custom_artist_images")
    }

    private fun ZipEntry.isCustomArtistPrefEntry(): Boolean {
        return name.startsWith(CUSTOM_ARTISTS_PATH) &&
                name.contains("prefs") &&
                getFileName() == "custom_artist_image.xml"
    }

    private fun ZipEntry.getFileName(): String {
        return name.substring(name.lastIndexOf(File.separator) + 1)
    }

    private fun ZipInputStream.readCheckedEntry(restoredBytes: Long): ByteArray {
        val output = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var entryBytes = 0L
        while (true) {
            val count = read(buffer)
            if (count < 0) break
            entryBytes += count
            if (entryBytes > MAX_BACKUP_ENTRY_BYTES || restoredBytes + entryBytes > MAX_BACKUP_TOTAL_BYTES) {
                throw IOException("Backup exceeds the allowed restore size")
            }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }

    fun getTimeStamp(): String {
        return SimpleDateFormat("dd-MMM yyyy HHmmss", Locale.getDefault()).format(Date())
    }

    private const val MAX_BACKUP_ENTRIES = 1_000
    private const val MAX_BACKUP_ENTRY_BYTES = 25L * 1024 * 1024
    private const val MAX_BACKUP_TOTAL_BYTES = 100L * 1024 * 1024

    private val allowedPreferenceFiles = setOf(
        "${BuildConfig.APPLICATION_ID}_preferences.xml",
        "$THEME_PREFS_KEY_DEFAULT.xml"
    )
}

data class ZipItem(val filePath: String, val zipPath: String)

fun CharSequence.sanitize(): String {
    return toString().replace("/", "_")
        .replace(":", "_")
        .replace("*", "_")
        .replace("?", "_")
        .replace("\"", "_")
        .replace("<", "_")
        .replace(">", "_")
        .replace("|", "_")
        .replace("\\", "_")
        .replace("&", "_")
}

fun String.child(child: String): String {
    return this + File.separator + child
}

enum class BackupContent {
    SETTINGS,
    USER_IMAGES,
    CUSTOM_ARTIST_IMAGES,
    PLAYLISTS
}
