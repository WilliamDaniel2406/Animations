package com.daniel2406.animation

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import androidx.annotation.DimenRes

inline fun getValueAnimator(
        forward: Boolean = true,
        duration: Long,
        interpolator: TimeInterpolator,
        crossinline updateListener: (progress: Float) -> Unit
): ValueAnimator {
    val a =
            if (forward) ValueAnimator.ofFloat(0f, 1f)
            else ValueAnimator.ofFloat(1f, 0f)
    a.addUpdateListener { updateListener(it.animatedValue as Float) }
    a.duration = duration
    a.interpolator = interpolator
    return a
}

inline val Int.dp: Int
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).toInt()

inline val Context.screenWidth: Int
    get() = Point().also { (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getSize(it) }.x
inline val View.screenWidth: Int
    get() = context!!.screenWidth


fun Any.bindDimen(context: Context, @DimenRes id: Int) = lazy(LazyThreadSafetyMode.NONE) {
    context.resources.getDimension(id)
}
