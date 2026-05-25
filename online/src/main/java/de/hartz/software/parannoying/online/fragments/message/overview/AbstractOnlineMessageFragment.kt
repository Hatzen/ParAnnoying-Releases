package de.hartz.software.parannoying.online.fragments.message.overview

import android.os.Bundle
import de.hartz.software.parannoying.online.fragments.AbstractMainFragment

abstract class AbstractOnlineMessageFragment: AbstractMainFragment() {

    private val KEY_MESSAGE_ID: String = "KEY_MESSAGE_ID"

    var selectedMessageIds = mutableListOf<Long>()

    fun clearAndSetSelectedMessageIds(id: Long) {
        clearAndSetSelectedMessageIds(arrayListOf(id))
    }

    fun clearAndSetSelectedMessageIds(ids: ArrayList<Long>) {
        selectedMessageIds = ids
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // TODO: Do we need a specific list for every fragment? (Can we return to a different fragment for example?)
        outState.putSerializable(KEY_MESSAGE_ID, ArrayList(selectedMessageIds))
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        val saved = savedInstanceState?.getSerializable(KEY_MESSAGE_ID) as java.util.ArrayList<Long>?
        if (saved != null) {
            clearAndSetSelectedMessageIds(saved)
        }
    }
}