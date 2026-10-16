package code.name.monkey.mousemusic.views

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.withStyledAttributes
import androidx.core.view.isVisible
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.appthemehelper.util.ColorUtil
import code.name.monkey.mousemusic.R
import code.name.monkey.mousemusic.databinding.ItemPermissionBinding
import code.name.monkey.mousemusic.extensions.accentOutlineColor

class PermissionItem @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = -1,
    defStyleRes: Int = -1
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private var binding: ItemPermissionBinding
    init {
        binding = ItemPermissionBinding.inflate(LayoutInflater.from(context), this, true)

        context.withStyledAttributes(attrs, R.styleable.PermissionItem, 0, 0) {
            binding.title.text = getText(R.styleable.PermissionItem_permissionTitle)
            binding.summary.text = getText(R.styleable.PermissionItem_permissionTitleSubTitle)
            binding.button.text = getText(R.styleable.PermissionItem_permissionButtonTitle)
            binding.permissionIcon.setImageResource(
                getResourceId(R.styleable.PermissionItem_permissionIcon, R.drawable.ic_album)
            )
            val color = ThemeStore.accentColor(context)
            binding.permissionIcon.backgroundTintList =
                ColorStateList.valueOf(ColorUtil.withAlpha(color, 0.22f))
            binding.permissionIcon.imageTintList = ColorStateList.valueOf(color)

            if (!isInEditMode) binding.button.accentOutlineColor()
        }
    }

    fun setButtonClick(callBack: () -> Unit) {
        binding.button.setOnClickListener { callBack() }
    }

    fun setGranted(granted: Boolean) {
        binding.button.isVisible = !granted
        binding.checkImage.isVisible = granted
        isActivated = granted
    }
}
