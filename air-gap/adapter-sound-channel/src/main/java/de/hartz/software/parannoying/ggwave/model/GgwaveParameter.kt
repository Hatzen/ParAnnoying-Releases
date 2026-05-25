package de.hartz.software.parannoying.ggwave.model

class GgwaveParameter {
    // payload length
    var payloadLength   :  Int
    // capture sample rate
    var sampleRateInp :  Float
    // playback sample rate
    var sampleRateOut :  Float
    // number of samples per audio frame
    var samplesPerFrame :  Int
    // sound marker detection threshold
    var soundMarkerThreshold: Float
    // format of the captured audio samples
    var sampleFormatInp: GgwaveSampleFormat
    // format of the playback audio samples
    var sampleFormatOut: GgwaveSampleFormat

    init {
        payloadLength = -1
        sampleRateInp = 48000.0f
        sampleRateOut = 48000.0f
        samplesPerFrame = 1024
        soundMarkerThreshold = 3.0f
        // Different then default params.
        sampleFormatInp = GgwaveSampleFormat.GGWAVE_SAMPLE_FORMAT_I16
        sampleFormatOut = GgwaveSampleFormat.GGWAVE_SAMPLE_FORMAT_I16
    }

}