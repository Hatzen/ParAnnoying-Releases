package de.hartz.software.parannoying.core.model.domain

class DeviceRole(var roleId: Long = UNKNOWN) {
    companion object {
        const val UNKNOWN: Long = -1
        const val UNDEFINED: Long = 1
        const val ONLINE: Long = 2
        const val OFFLINE: Long = 3
    }

    var persistenceId: Long = 0

    constructor() : this(UNKNOWN)

    override fun equals(other: Any?): Boolean {
        if (other is DeviceRole) {
            return other.roleId == this.roleId
        }
        return super.equals(other)
    }
}