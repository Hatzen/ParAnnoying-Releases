package de.hartz.software.parannoying.core.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.iterator
import de.hartz.software.parannoying.core.R

class FancyToolbar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.toolbarStyle
) : Toolbar(context, attrs, defStyleAttr) {

    init {
        setBackgroundColor(
            ContextCompat.getColor(
                context,
                R.color.colorPrimary
            )
        ) // Avoid crashes in XML preview
        setTitleTextColor(ContextCompat.getColor(context, R.color.colorWhite))

        if (isInEditMode) {
            setBackgroundColor(0x00FF0000.toInt()) // Light gray background for preview
        }
    }

    /**
     * Adds a menu item with an optional animation
     */
    fun addMenuItem(id: Int, title: String, drawable: Drawable, animated: Boolean = true) {
        menu.add(0, id, 0, title).apply {
            setIcon(drawable)
            setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        }
        if (animated) animateMenuItem(id, true)
    }

    /**
     * Removes a menu item with animation
     */
    fun removeMenuItem(id: Int, animated: Boolean = true) {
        val item = menu.findItem(id)
        if (item != null) {
            if (animated) {
                animateMenuItem(id, false) {
                    menu.removeItem(id)
                }
            } else {
                menu.removeItem(id)
            }
        }
    }

    fun clearAll(animated: Boolean = true) {
        menu.iterator().forEach { removeMenuItem(it.itemId, animated) }
    }

    /**
     * Fades menu items in/out
     */
    private fun animateMenuItem(id: Int, fadeIn: Boolean, onEnd: (() -> Unit)? = null) {
        val itemView = findViewById<View>(id) ?: return
        itemView.clearAnimation()
        val animation = AlphaAnimation(if (fadeIn) 0f else 1f, if (fadeIn) 1f else 0f).apply {
            duration = 300
            setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationEnd(anim: Animation?) {
                    onEnd?.invoke()
                }
                override fun onAnimationStart(anim: Animation?) {}
                override fun onAnimationRepeat(anim: Animation?) {}
            })
        }
        itemView.startAnimation(animation)
    }
}