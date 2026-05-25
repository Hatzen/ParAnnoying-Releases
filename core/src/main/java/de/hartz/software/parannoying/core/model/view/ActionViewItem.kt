package de.hartz.software.parannoying.core.model.view

import android.graphics.drawable.Drawable

data class ActionViewItem(
        val text: String,
        val action: () -> Unit,
        val icon: Drawable? = null,
        val description: String? = null)