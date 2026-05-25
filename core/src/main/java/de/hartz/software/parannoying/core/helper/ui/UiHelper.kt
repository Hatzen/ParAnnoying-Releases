package de.hartz.software.parannoying.core.helper.ui

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar
import de.hartz.software.parannoying.core.R
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Timer
import kotlin.concurrent.fixedRateTimer


object UiHelper {

    // https://stackoverflow.com/a/42670723/8524651
    fun areSystemAnimationsEnabled(context: Context): Boolean {
        val duration = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.ANIMATOR_DURATION_SCALE, 1f)
        val transition = Settings.Global.getFloat(
                    context.getContentResolver(),
                    Settings.Global.TRANSITION_ANIMATION_SCALE, 1f)

        return duration != 0f && transition != 0f
    }
    
    // TODO: Return object so it can only started once. And also can end to a specific condition instead of a number of runs..
    fun initImageSwitcher(activity: Activity, targetView: ImageView, drawableArray: Array<Drawable>, maxCount: Int = -1) : Timer {
        var count = 0
        var index = 0
        return fixedRateTimer("default", false, 0, 2000L){
            activity.runOnUiThread {
                imageViewAnimatedChange(activity, targetView, drawableArray[index])
                index = (index + 1) % drawableArray.size
                count++
                if (maxCount != -1 && count == maxCount) {
                    this.cancel()
                }
            }
        }
    }

    fun setRoundedBackground(context: Context, view: View) {
        // TODO: Maybe better use corners like shapeimageview?
        val radius = 15.0f
        val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()
        val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
        shapeDrawable.setFillColor(ContextCompat.getColorStateList(context , R.color.colorPrimaryDark))
        shapeDrawable.setStroke(2.0f, ContextCompat.getColor( context, R.color.colorPrimary))
        ViewCompat.setBackground(view, shapeDrawable)
    }

    fun showSnackbarTop(text: String, relatedView: View) {
        // https://stackoverflow.com/a/31746370/8524651
        val snack: Snackbar = Snackbar.make(relatedView, text, Snackbar.LENGTH_LONG)
        val view = snack.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snack.show()
    }

    fun imageViewAnimatedChange(c: Context, v: ImageView, new_image: Drawable) {
        // TODO: Check that this is working.
        v.clearAnimation()
        val anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out)
        val anim_in = AnimationUtils.loadAnimation(c, android.R.anim.fade_in)
        anim_out.setAnimationListener(object : AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                v.setImageDrawable(new_image)
                anim_in.setAnimationListener(object : AnimationListener {
                    override fun onAnimationStart(animation: Animation) {}
                    override fun onAnimationRepeat(animation: Animation) {}
                    override fun onAnimationEnd(animation: Animation) {}
                })
                v.startAnimation(anim_in)
            }
        })
        v.startAnimation(anim_out)
    }

    fun addCopyOnLongClickListener(textView: TextView) {
        textView.setOnLongClickListener {
            val textToCopy = textView.text.toString()

            val clipboard = it.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", textToCopy)
            clipboard.setPrimaryClip(clip)

            showToastFromBackgroundTask(it.context, "Copied to clipboard")

            true // consume the long click
        }
    }

    fun showToastFromBackgroundTask(context: Context, text: String) {
        val handler = Handler(Looper.getMainLooper())
        handler.post { Toast.makeText(context, text, Toast.LENGTH_SHORT).show() }
    }

    // https://stackoverflow.com/a/13249391/8524651
    fun getTrafficLightColor(value: Float, context: Context): Int {
        val ratio: Float

        var startColor = ContextCompat.getColor(context, R.color.colorRed)
        var endColor = ContextCompat.getColor(context, R.color.colorYellow)
        if (value > 0.5) {
            ratio =  2 * (value - 0.5f)
            startColor = ContextCompat.getColor(context, R.color.colorYellow)
            endColor = ContextCompat.getColor(context, R.color.colorPrimaryDark)
        } else {
            ratio = 2 * value
        }

        if (ratio < 0 || ratio > 1) {
            throw Exception("Seed out of range")
        }

        return ColorUtils.blendARGB(startColor, endColor, ratio.toFloat())
    }

    fun getString(id: Int, context: Context) : String {
        return context.resources.getString(id)
    }

    fun getDateWithoutYear(millis: Long) : String {
        val date = Date(millis)
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
        return dateFormat.format(date)
    }

    fun getDateWithYear(millis: Long) : String {
        val date = Date(millis)
        val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT).format(date)
        return dateFormat.format(date)
    }

    fun getDateWithTime(millis: Long) : String {
        val date = Date(millis)
        val dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date)
        return dateFormat.format(date)
    }

    fun isSameDay(millis1: Long, millis2: Long) : Boolean {
        val date1 = Date(millis1)
        val date2 = Date(millis2)
        val fmt = SimpleDateFormat("yyyyMMdd")
        return fmt.format(date1) == fmt.format(date2)
    }

}