package de.hartz.software.parannoying.core.model.domain.welcome

import android.content.Context
import android.graphics.drawable.Drawable
import de.hartz.software.parannoying.core.helper.ui.IconTemplate
import kotlin.reflect.KFunction2

data class ChannelExplanation(
        val name: String,
        val description: String,
        val usability: Int,
        val security: Int,
        val throughput: Int,
        val icon: KFunction2<Context, IconTemplate, Drawable>
)