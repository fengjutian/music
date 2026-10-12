package code.name.monkey.mousemusic.model

import androidx.annotation.StringRes
import code.name.monkey.mousemusic.HomeSection

data class Home(
    val arrayList: List<Any>,
    @HomeSection
    val homeSection: Int,
    @StringRes
    val titleRes: Int
)