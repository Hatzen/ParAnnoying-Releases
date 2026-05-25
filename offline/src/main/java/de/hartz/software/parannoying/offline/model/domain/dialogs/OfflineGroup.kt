package de.hartz.software.parannoying.offline.model.domain.dialogs

import android.content.Context
import com.stfalcon.chatkit.commons.models.IUser
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.offline.helper.security.ConstantKeys
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.KeyConverter
import java.security.PrivateKey

open class OfflineGroup(
        nickname: String,
        hash: String
): SimpleDialog(nickname, hash), IUser{

    override val className: String
        get() = javaClass.simpleName

    lateinit var groupId: String

    fun extractPrivateKey(context: Context, securityInterfaceHolder: SecurityInterfaceHolder): PrivateKey {
        val publicKey = encryptionKeyCloakForUser.encryptionKey
        val key = KeyConverter().convertToDatabaseValue(publicKey)!!
        return ConstantKeys(context, securityInterfaceHolder.asymmetricEncryptionHelper)
            .getKeyByPublicKeyString(key).private
    }


    override fun getUsers(): MutableList<out IUser> {
        return mutableListOf(this)
    }

    override fun getName(): String {
        return nickname
    }

    override fun getAvatar(): String {
        // TODO: This will get called to display the user of the last message
        // dialogStyle.isDialogMessageAvatarEnabled() in com.stfalcon.chatkit.dialogs.DialogsListAdapter
        return getDialogPhoto()
    }

}
