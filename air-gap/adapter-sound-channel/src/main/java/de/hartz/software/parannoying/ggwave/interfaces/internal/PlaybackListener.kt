package de.hartz.software.parannoying.ggwave.interfaces.internal

interface PlaybackListener {
    fun process(progress: ByteArray)
    fun onProgress(progress: Int)
    fun onCompletion()
}