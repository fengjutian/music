package code.name.monkey.mousemusic

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import cat.ereza.customactivityoncrash.config.CaocConfig
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.VersionUtils
import code.name.monkey.mousemusic.activities.ErrorActivity
import code.name.monkey.mousemusic.activities.MainActivity
import code.name.monkey.mousemusic.appshortcuts.DynamicShortcutManager
import code.name.monkey.mousemusic.helper.WallpaperAccentManager
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {

    private val wallpaperAccentManager = WallpaperAccentManager(this)

    override fun onCreate() {
        super.onCreate()
        instance = this
        configureDefaultLocale()

        startKoin {
            androidContext(this@App)
            modules(appModules)
        }
        // default theme
        if (!ThemeStore.isConfigured(this, 3)) {
            ThemeStore.editTheme(this)
                .accentColorRes(R.color.netease_brand)
                .coloredNavigationBar(true)
                .commit()
        }
        wallpaperAccentManager.init()

        if (VersionUtils.hasNougatMR())
            DynamicShortcutManager(this).initDynamicShortcuts()

        // setting Error activity
        CaocConfig.Builder.create().errorActivity(ErrorActivity::class.java)
            .restartActivity(MainActivity::class.java).apply()

        // Set Default values for now playing preferences
        // This will reduce startup time for now playing settings fragment as Preference listener of AbsSlidingMusicPanelActivity won't be called
        PreferenceManager.setDefaultValues(this, R.xml.pref_now_playing_screen, false)
    }

    private fun configureDefaultLocale() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val hasLanguagePreference = preferences.contains(LANGUAGE_NAME)
        val hasApplicationLocale =
            AppCompatDelegate.getApplicationLocales().toLanguageTags().isNotEmpty()
        if (!hasLanguagePreference && !hasApplicationLocale) {
            preferences.edit {
                putString(LANGUAGE_NAME, DEFAULT_LANGUAGE_TAG)
                putBoolean(LOCALE_AUTO_STORE_ENABLED, true)
            }
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(DEFAULT_LANGUAGE_TAG)
            )
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        wallpaperAccentManager.release()
    }

    companion object {
        private const val DEFAULT_LANGUAGE_TAG = "zh-CN"
        private var instance: App? = null

        fun getContext(): App {
            return instance!!
        }

        fun isProVersion(): Boolean {
            return true
        }
    }
}
