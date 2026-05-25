package de.hartz.software.parannoying.core.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView


/**
 * ImageView with mask what described with Bézier Curves
 *
 * Copied so we dont need the full dependency for online device.
 * Source: https://github.com/stfalcon-studio/ChatKit/blob/master/chatkit/src/main/java/com/stfalcon/chatkit/utils/ShapeImageView.java
 */
class ShapeImageView : AppCompatImageView {
    private lateinit var path: Path

    constructor(context: Context) : super(context) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        path = Path()
        val halfWidth = w.toFloat() / 2f
        val firstParam = w.toFloat() * 0.1f
        val secondParam = w.toFloat() * 0.8875f

        //Bézier Curves
        path.moveTo(halfWidth, w.toFloat())
        path.cubicTo(firstParam, w.toFloat(), 0f, secondParam, 0f, halfWidth)
        path.cubicTo(0f, firstParam, firstParam, 0f, halfWidth, 0f)
        path.cubicTo(secondParam, 0f, w.toFloat(), firstParam, w.toFloat(), halfWidth)
        path.cubicTo(w.toFloat(), secondParam, secondParam, w.toFloat(), halfWidth, w.toFloat())
        path.close()
    }

    override fun onDraw(canvas: Canvas) {
        if (path.isEmpty()) {
            super.onDraw(canvas)
            return
        }
        val saveCount: Int = canvas.save()
        canvas.clipPath(path)
        super.onDraw(canvas)
        canvas.restoreToCount(saveCount)
    }
}