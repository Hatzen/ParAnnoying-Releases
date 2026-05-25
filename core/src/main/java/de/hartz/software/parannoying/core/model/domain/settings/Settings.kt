package de.hartz.software.parannoying.core.model.domain.settings

abstract class Settings<H> where H: HiddenSettings {
    var screenSaver: Boolean = false
    var useFakeEntryActivity: Boolean = false
    var allowedChannels: ArrayList<Channels> = ArrayList()
    var primaryChannel: Channels = Channels.TEXT

    // Qrcode / Video  (maybe nfc in future?)
    var useWatermark: Boolean = false
    var useAutomaticConfirmation: Boolean = false
    var videoSpeed: QrVideoSpeed? = QrVideoSpeed.NORMAL
    
    // Bluetooth
    var storedBluetoothMac: String? = null
    var storeLastBluetoothMac: Boolean = false
    // Sd card; default first usable.
    var choosenSdCard: String? = null
    // Audio
    var soundSpectrumAndSpeed: SoundSpectrumAndSpeed = SoundSpectrumAndSpeed.ULTRASOUND_FAST

    open lateinit var hiddenSettings: H

}