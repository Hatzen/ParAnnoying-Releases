package de.hartz.software.parannoying.offline.model.persistence.settings

import de.hartz.software.parannoying.core.model.persistence.realm.SingletonRealmObject
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class SettingsPersistence : SingletonRealmObject, RealmObject() {
    @PrimaryKey
    override var constantId: Long = 1

    var screenSaver: Boolean? = true
    lateinit var allowedChannels: RealmList<ChannelsPersistence>
    var primaryChannel: ChannelsPersistence? = null

    var useWatermark: Boolean? = false
    var useAutomaticConfirmation: Boolean? = false
    var videoSpeed: QrVideoSpeedPersistence? = null
    // Bluetooth
    var storedBluetoothMac: String? = null
    var storeLastBluetoothMac: Boolean? = false
    // Sd card; default first usable.
    var choosenSdCard: String? = null
    // Audio
    var soundSpectrumAndSpeed: SoundSpectrumAndSpeedPersistence? = null

    var messageSecurity: MessageSecurityPersistence? = null
    var syncAllMessages: Boolean? = false
    var useFakeEntryActivity: Boolean? = false

    var hiddenSettings: HiddenSettingsPersistence? = null
}