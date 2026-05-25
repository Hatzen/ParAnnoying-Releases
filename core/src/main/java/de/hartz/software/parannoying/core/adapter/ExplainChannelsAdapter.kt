package de.hartz.software.parannoying.core.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import com.mikepenz.iconics.view.IconicsImageView
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.model.domain.welcome.listOfChannels

class ExplainChannelsAdapter(private val context: Context) : RecyclerView.Adapter<ExplainChannelsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewItem = LayoutInflater.from(context)
                .inflate(de.hartz.software.parannoying.core.R.layout.item_channels_explained, parent, false)
        return ViewHolder(viewItem)
    }

    private fun addDots(count: Int, parent: ViewGroup) {
        parent.removeAllViews()
        // TODO: Evaluate (0..count).forEach
        repeat((0..count).count()) {
            val view = IconicsImageView(context)
            val drawable = IconicsDrawable(context, FontAwesome.Icon.faw_dot_circle)
            drawable.colorInt = context.resources.getColor(R.color.colorWhite)
            drawable.sizeDp = 28
            drawable.paddingDp = 5
            view.icon = drawable
            parent.addView(view)
        }
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val channelExplanation = listOfChannels[position]
        holder.description.text = channelExplanation.description

        addDots(channelExplanation.usability, holder.usabiliy )
        addDots(channelExplanation.security, holder.security)
        addDots(channelExplanation.throughput, holder.throughput)

        holder.background_channel.icon = channelExplanation.icon(context, IconHelper.LARGE_ICON_PRIMARY) as IconicsDrawable
    }

    override fun getItemCount(): Int {
        return listOfChannels.size
    }

    class ViewHolder(viewItem: View) : RecyclerView.ViewHolder(viewItem) {

        val description: TextView
        val background_channel: IconicsImageView
        val usabiliy: LinearLayout
        val security: LinearLayout
        val throughput: LinearLayout

        init {
            description = viewItem.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.description)
            usabiliy = viewItem.findViewById<LinearLayout>(de.hartz.software.parannoying.core.R.id.usabiliy)
            security = viewItem.findViewById<LinearLayout>(de.hartz.software.parannoying.core.R.id.security)
            throughput = viewItem.findViewById<LinearLayout>(de.hartz.software.parannoying.core.R.id.throughput)
            background_channel = viewItem.findViewById<IconicsImageView>(de.hartz.software.parannoying.core.R.id.background_channel)
        }
    }
}