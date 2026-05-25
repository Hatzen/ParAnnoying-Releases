package de.hartz.software.parannoying.offline.model.domain.dialogs

import de.hartz.software.parannoying.offline.model.domain.OfflineKeyPair
import de.hartz.software.parannoying.offline.model.domain.dialogs.User


open class CurrentUser(
        nickname: String,
        hash: String
): User(nickname, hash) {

    lateinit var signKey: OfflineKeyPair

}