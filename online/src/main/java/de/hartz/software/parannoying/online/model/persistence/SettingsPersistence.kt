package de.hartz.software.parannoying.online.model.persistence

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

    lateinit var serverConfigs: RealmList<ServerConfigPersistence>

    var logEncryptedMessages: Boolean? = true
    var reportErrorOnline: Boolean? = true
    // TODO: Move useGoogleApi here.
    // var useGoogleApi: Boolean? = false

    var useFakeEntryActivity: Boolean? = false


    // TODO: Do we need hiddensettings for online device?
    // var hiddenSettings: HiddenSettingsPersistence
}