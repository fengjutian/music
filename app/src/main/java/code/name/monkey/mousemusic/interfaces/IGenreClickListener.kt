package code.name.monkey.mousemusic.interfaces

import android.view.View
import code.name.monkey.mousemusic.model.Genre

interface IGenreClickListener {
    fun onClickGenre(genre: Genre, view: View)
}