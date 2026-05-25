package de.hartz.software.parannoying.offline.adapters.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import de.hartz.software.parannoying.core.interfaces.di.security.HashHelper
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser


class UserOverviewAdapter(var values : List<DecryptionKeyCloakForUser>, val hashHelper: HashHelper, context:Context)
    :ArrayAdapter<DecryptionKeyCloakForUser>(context, R.layout.row_unconfirmed_user_id) {

    override fun getCount(): Int {
        return values.size
    }

    override fun getView(position:Int, convertView:View?, parent:ViewGroup): View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val rowView = inflater.inflate(R.layout.row_unconfirmed_user_id, parent, false)

        val unconfirmedUserId = values[position]

        rowView.findViewById<TextView>(R.id.label).text = hashHelper.getStringHashForUi(unconfirmedUserId.persistenceId.toString())

        return rowView
    }

}