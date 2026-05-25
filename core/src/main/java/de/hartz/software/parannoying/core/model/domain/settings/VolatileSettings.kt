package de.hartz.software.parannoying.core.model.domain.settings

open class VolatileSettings: Settings<HiddenSettings>() {

    init {
        hiddenSettings = HiddenSettings()
    }
}