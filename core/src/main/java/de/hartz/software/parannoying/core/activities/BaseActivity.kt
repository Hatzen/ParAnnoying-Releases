package de.hartz.software.parannoying.core.activities

import android.os.Bundle
import android.os.PersistableBundle
import com.github.omadahealth.lollipin.lib.PinCompatActivity

abstract class BaseActivity : PinCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeTransition()
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        changeTransition()
    }

    override fun onPause() {
        super.onPause()
        changeTransition()
    }

    override fun onResume() {
        super.onResume()
        changeTransition()
    }

    private fun changeTransition() {
        //https://stackoverflow.com/questions/3389501/activity-transition-in-android
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}