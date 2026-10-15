package code.name.monkey.mousemusic.extensions

import androidx.core.view.WindowInsetsCompat
import code.name.monkey.mousemusic.util.PreferenceUtil
import code.name.monkey.mousemusic.util.MuseUtil

fun WindowInsetsCompat?.getBottomInsets(): Int {
    return if (PreferenceUtil.isFullScreenMode) {
        return 0
    } else {
        this?.getInsets(WindowInsetsCompat.Type.systemBars())?.bottom ?: MuseUtil.navigationBarHeight
    }
}
