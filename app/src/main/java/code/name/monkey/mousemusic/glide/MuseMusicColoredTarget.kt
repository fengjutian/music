package code.name.monkey.mousemusic.glide

import android.graphics.drawable.Drawable
import android.widget.ImageView
import code.name.monkey.mousemusic.App
import code.name.monkey.mousemusic.extensions.colorControlNormal
import code.name.monkey.mousemusic.glide.palette.BitmapPaletteTarget
import code.name.monkey.mousemusic.glide.palette.BitmapPaletteWrapper
import code.name.monkey.mousemusic.util.color.MediaNotificationProcessor
import com.bumptech.glide.request.transition.Transition

abstract class MuseMusicColoredTarget(view: ImageView) : BitmapPaletteTarget(view) {

    protected val defaultFooterColor: Int
        get() = getView().context.colorControlNormal()

    abstract fun onColorReady(colors: MediaNotificationProcessor)

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        onColorReady(MediaNotificationProcessor.errorColor(App.getContext()))
    }

    override fun onResourceReady(
        resource: BitmapPaletteWrapper,
        transition: Transition<in BitmapPaletteWrapper>?
    ) {
        super.onResourceReady(resource, transition)
        MediaNotificationProcessor(App.getContext()).getPaletteAsync({
            onColorReady(it)
        }, resource.bitmap)
    }
}
