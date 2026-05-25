package de.hartz.software.parannoying.offline.model.domain

import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage

data class SearchResult(val text: String, val type: Type, val baseDialog: BaseDialog, val message: AbstractMessage? = null)

enum class Type {
    MESSAGE, USER
}