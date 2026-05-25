package de.hartz.software.parannoying.offline.model.domain.events

import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog

class UserEvent(eventText: String, var relatedUser: BaseDialog): BaseEvent(eventText) {

}