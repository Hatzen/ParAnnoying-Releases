package de.hartz.software.parannoying.offline.helper.security.serializer

import de.hartz.software.parannoying.offline.model.domain.DecryptionKeyCloakForUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage

abstract class AbstractDeserializer<T: AbstractMessage> {

    abstract fun decryptMessage(data: String, sourceUser: SimpleDialog, symmetricToken: String): T

    abstract fun decryptMessage(data: String, keys: DecryptionKeyCloakForUser, symmetricToken: String): Pair<T, SimpleDialog>

}
