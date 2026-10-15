package code.name.monkey.mousemusic.util.theme

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDelegate
import code.name.monkey.mousemusic.R
import code.name.monkey.mousemusic.extensions.generalThemeValue
import code.name.monkey.mousemusic.util.PreferenceUtil
import code.name.monkey.mousemusic.util.theme.ThemeMode.*

@StyleRes
fun Context.getThemeResValue(): Int =
    if (PreferenceUtil.materialYou) {
        if (generalThemeValue == BLACK) R.style.Theme_MuseMusic_MD3_Black
        else R.style.Theme_MuseMusic_MD3
    } else {
        when (generalThemeValue) {
            LIGHT -> R.style.Theme_MuseMusic_Light
            DARK -> R.style.Theme_MuseMusic_Base
            BLACK -> R.style.Theme_MuseMusic_Black
            AUTO -> R.style.Theme_MuseMusic_FollowSystem
        }
    }

fun Context.getNightMode(): Int = when (generalThemeValue) {
    LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
    DARK -> AppCompatDelegate.MODE_NIGHT_YES
    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
}