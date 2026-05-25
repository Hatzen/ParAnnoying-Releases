package de.hartz.software.parannoying.core.fragments.welcome

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.viewpager2.widget.ViewPager2
import de.hartz.software.parannoying.core.adapter.ExplainChannelsAdapter


class WelcomeChannelsExplainedFragment: WelcomeBaseFragment() {

    lateinit var pager: ViewPager2
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(de.hartz.software.parannoying.core.R.layout.welcome_page_channels_explained, null, false)
        pager = view.findViewById(de.hartz.software.parannoying.core.R.id.pager)

        val adapter = ExplainChannelsAdapter(requireContext())
        pager.orientation = ViewPager2.ORIENTATION_VERTICAL
        pager.adapter = adapter

        return view
    }

    override fun onResume() {
        super.onResume()
        val pixels = pager.height / 8
        ValueAnimator.ofInt(0, pixels).apply {
            duration = 350L
            interpolator = DecelerateInterpolator()
            repeatCount = 5
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                pager.scrollY = it.animatedValue as Int
            }
        }.start()
    }

    override fun canMoveFurther(): Boolean {
        return super.canMoveFurther()
    }


    override fun backgroundColor(): Int {
        return de.hartz.software.parannoying.core.R.color.colorPrimary
    }
}

