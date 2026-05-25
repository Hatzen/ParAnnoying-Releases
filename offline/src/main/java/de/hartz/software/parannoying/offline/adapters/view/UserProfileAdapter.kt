package de.hartz.software.parannoying.offline.adapters.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.iconics.view.IconicsImageView
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.core.views.ShapeImageView
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.activities.offline.userinfo.UserProfileItem

class UserProfileAdapter(private val items: List<UserProfileItem>, private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int) = when (items[position]) {
        is UserProfileItem.ProfileImage -> 0
        is UserProfileItem.SectionHeader -> 1
        is UserProfileItem.StaticInfo -> 2
        is UserProfileItem.Action -> 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> ImageHolder(
                inflater.inflate(R.layout.item_user_info_image, parent, false),
                parent.context
            )
            1 -> SectionHeaderHolder(
                inflater.inflate(R.layout.item_user_info_section_header, parent, false)
            )
            2 -> StaticInfoHolder(inflater.inflate(R.layout.item_user_info_static, parent, false))
            3 -> ActionHolder(inflater.inflate(R.layout.item_main_action, parent, false), parent.context)
            // 4 -> DynamicInfoHolder(inflater.inflate(R.layout.item_dialog, parent, false))
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is UserProfileItem.StaticInfo -> (holder as StaticInfoHolder).bind(item)
            is UserProfileItem.Action -> (holder as ActionHolder).bind(item)
            is UserProfileItem.ProfileImage -> (holder as ImageHolder).bind(item)
            is UserProfileItem.SectionHeader -> (holder as SectionHeaderHolder).bind(item)
        }
    }


    class SectionHeaderHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: UserProfileItem.SectionHeader) {
            itemView.findViewById<TextView>(R.id.headerTitle).text = item.title
        }
    }

    class StaticInfoHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(item: UserProfileItem.StaticInfo) {
            itemView.findViewById<TextView>(R.id.label).text = item.label
            val valueTextview = itemView.findViewById<TextView>(R.id.value)
            valueTextview.text = item.value
            UiHelper.addCopyOnLongClickListener(valueTextview)
        }
    }

    class ActionHolder(view: View, val context: Context) : RecyclerView.ViewHolder(view) {
        fun bind(item: UserProfileItem.Action) {
            val entry = item
            itemView.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.text).text = entry.text

            if (entry.icon == null) {
                itemView.findViewById<IconicsImageView>(de.hartz.software.parannoying.core.R.id.icon).visibility = View.INVISIBLE
            } else {
                itemView.findViewById<ImageView>(de.hartz.software.parannoying.core.R.id.icon).setImageDrawable(entry.icon)
            }

            if (entry.description == null) {
                itemView.findViewById<IconicsImageView>(de.hartz.software.parannoying.core.R.id.info).visibility = View.GONE
            } else {
                itemView.findViewById<IconicsImageView>(de.hartz.software.parannoying.core.R.id.info).setOnClickListener {
                    DialogHelper.showDialog(context, "Info", entry.description!!)
                }
            }

            itemView.setOnClickListener { view ->
                entry.action()
            }
        }
    }

    class ImageHolder(view: View, val context: Context) : RecyclerView.ViewHolder(view) {
        fun bind(item: UserProfileItem.ProfileImage) {
            val bitmap = IOHelper.getProfilePictureForUser(item.seed, context)
            itemView.findViewById<ShapeImageView>(R.id.profile_picture).setImageBitmap(bitmap)
        }
    }
}