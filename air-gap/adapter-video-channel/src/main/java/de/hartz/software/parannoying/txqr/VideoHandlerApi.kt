package de.hartz.software.parannoying.txqr

import txqr.Txqr
import java.io.File
import java.io.FileOutputStream

// https://github.com/divan/txqr-reader/blob/master/txqr-reader/QRScannerController.swift
class VideoHandlerApi {

    val decoder = Txqr.newDecoder()

    fun createGif(data: String, speed: Int): File {
        val bytes = if (speed == 0) {
             Txqr.newGifCreator().createGif(data)
        } else if (speed < 0) {
            Txqr.newGifCreator().createGifLargerAndSlower(data)
        } else if (speed > 0) {
            Txqr.newGifCreator().createGifSmallAndFast(data)
        } else {
            throw RuntimeException("no possibles")
        }
        val file = File.createTempFile("test", "gif")
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            val fos = FileOutputStream(file);
            fos.write(bytes);
            fos.close();
        } catch (e: Exception) {
            throw e
        }
        return file
    }

    fun decode(chunk: String): String? {
        if (decoder.isCompleted) {
            throw RuntimeException("please dont overuse cpu..")
        }
        try {
            decoder?.decode(chunk)
        } catch (exception: Exception) {
            throw exception
        }

        if (decoder.isCompleted) {
             return decoder.data()
        }
        return null
    }
}