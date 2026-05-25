package de.hartz.software.parannoying.core.views

import android.content.Context
import android.util.AttributeSet
import android.widget.ListView

class GenericActionListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.toolbarStyle
) : ListView(context, attrs, defStyleAttr) {

    init {
        setDivider(null)
    }
}