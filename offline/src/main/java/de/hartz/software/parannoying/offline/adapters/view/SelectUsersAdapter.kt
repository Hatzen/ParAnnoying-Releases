package de.hartz.software.parannoying.offline.adapters.view

import android.content.Context
import android.content.Intent
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.iconics.view.IconicsTextView
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity
import de.hartz.software.parannoying.offline.activities.offline.userinfo.UserInfoActivity
import de.hartz.software.parannoying.offline.model.domain.dialogs.DummyUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.User

class SelectUsersAdapter(var data: List<User>, var selectedIds: ArrayList<Long>, var mContext: Context, var selectable: Boolean)
    : ArrayAdapter<User>(mContext, R.layout.item_selectable_user, data), View.OnClickListener, View.OnLongClickListener {
    private var lastPosition = -1

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val user: User = getItem(position)!!

        // Handle dummy users.
        if (user is DummyUser) {
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.item_unknown_user, parent, false)
            val name = convertView!!.findViewById<View>(R.id.name) as TextView
            name.text = user.hash
            return convertView
        }

        // Handle real users.
        val isDummyUserConvertView = convertView?.findViewById<View>(R.id.image) as ImageView? == null
        if (convertView == null || isDummyUserConvertView) {
            val inflater = LayoutInflater.from(context)
            convertView = inflater.inflate(R.layout.item_selectable_user, parent, false)
        }
        val name = convertView!!.findViewById<View>(R.id.name) as TextView
        val checked = convertView.findViewById<View>(R.id.checked) as IconicsTextView
        val image = convertView.findViewById<ImageView>(R.id.image)

        name.text = user.nickname
        image.setImageBitmap(IOHelper.getProfilePictureForUser(user.dialogPhoto, mContext))
        checked.visibility = View.GONE
        if (selectable && selectedIds.contains(user.persistenceId)) {
            toggleChecked(checked)
        }

        lastPosition = position
        convertView.tag = position
        if (selectable) {
            convertView.setOnClickListener(this)
            convertView.setOnLongClickListener(this)
        }
        return convertView
    }

    override fun onClick(v: View) {
        val position = v.tag as Int
        val user = getItem(position) as User

        if (selectedIds.contains(user.persistenceId)) {
            selectedIds.remove(user.persistenceId)
        } else {
            selectedIds.add(user.persistenceId)
        }
        val checkedIcon = v.findViewById<View>(R.id.checked)
        toggleChecked(checkedIcon)
    }

    override fun onLongClick(v: View): Boolean {
        val position = v.tag as Int
        val user = getItem(position) as User

        val passParams : (userIntent: Intent) -> Unit = {
            it.putExtra(ChatActivity.EXTRA_USER, user.persistenceId)
        }
        context.launchActivity<UserInfoActivity>(init = passParams)
        return true
    }

    private fun toggleChecked (view: View) {
        val transition: Transition = Fade()
        transition.setDuration(300)
        transition.addTarget(view)

        TransitionManager.beginDelayedTransition(view.parent as ViewGroup, transition)
        view.setVisibility(if (view.visibility == View.GONE) View.VISIBLE else View.GONE)
    }

}