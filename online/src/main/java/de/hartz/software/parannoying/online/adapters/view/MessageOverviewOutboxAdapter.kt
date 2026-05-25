package de.hartz.software.parannoying.online.adapters.view

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.view.IconicsTextView
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.UiHelper
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.model.OnlineStorage
import de.hartz.software.parannoying.online.model.domain.OutboxEncryptedMessage


class MessageOverviewOutboxAdapter(
    var values :  List<OutboxEncryptedMessage>,
    val securityInterfaceHolder: SecurityInterfaceHolder,
    val onlineStorage: OnlineStorage,
    context:Context) :ArrayAdapter<OutboxEncryptedMessage>(context, R.layout.row_send_encryptedmessage) {

    val dataSecurityHelper = DataSecurityHelper(securityInterfaceHolder)

    override fun getCount(): Int {
        return values.size
    }

    override fun getView(position:Int, convertView:View?, parent:ViewGroup):View {
        val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        // TODO: use viewholder pattern? Not really needed as there shouldnt be many messages..
        val rowView = inflater.inflate(R.layout.row_send_encryptedmessage, parent, false)

        val encryptedMessag = values[position]
        rowView.findViewById<TextView>(R.id.label).text = "Message " + securityInterfaceHolder.hashHelper.getStringHashForUi(encryptedMessag.message)

        val sendAtView = rowView.findViewById<TextView>(R.id.outbox_time)
        sendAtView.text = "{cmd-inbox}" + UiHelper.getDateWithTime(encryptedMessag.createdAt)

        val hash = encryptedMessag.targetUserHash
        rowView.findViewById<ImageView>(R.id.dialogAvatar).setImageBitmap(IOHelper.getProfilePictureForUserWithHashOnly(hash, context))

        val dialogType: IconicsTextView = rowView.findViewById(R.id.dialogType)
        // Setup type of group
        dialogType.let {
            if (dataSecurityHelper.isOnlineIdValid(hash)) {
                it.setTextColor(ResourcesCompat.getColor(context.getResources(), de.hartz.software.parannoying.core.R.color.colorPrimaryDark, null))
            } else {
                it.setTextColor(ResourcesCompat.getColor(context.getResources(), de.hartz.software.parannoying.core.R.color.gray, null))
            }

            // TODO: Unify with ChatOverviewAdapter
            Iconics.Builder().on(it).build()
            if (dataSecurityHelper.isOnlineIdForOnlineGroup(hash)) {
                it.text = "{faw-users} " +
                        hash
                        .replace(DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER, DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_SEPARATOR)
                                .split(DataSecurityHelper.ONLINE_GROUP_ONLINE_ID_TARGET_PREFIX_MARKER).size
            } else if (hash.startsWith(DataSecurityHelper.NOTIFICATION_ID_PREFIX_GROUP_OFFLINE)) {
                it.text = "{faw-users}"
            } else {
                if (hash == onlineStorage.onlineUserId) {
                    it.text = "{cmd-account-circle}"
                } else {
                    it.text = "{cmd-account}"
                }
            }
        }

        return rowView
    }

}