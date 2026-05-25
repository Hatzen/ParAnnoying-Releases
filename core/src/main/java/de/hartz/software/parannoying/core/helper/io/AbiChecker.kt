package de.hartz.software.parannoying.core.helper.io

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log


// https://stackoverflow.com/a/41607009/8524651
object AbiChecker {

    @SuppressLint("NewApi")
    fun is32BitAbi(): Boolean {
        val DEBUG_TAG_ARC = "Supported ABIS"

        val OSNumber = Integer.valueOf(Build.VERSION.SDK_INT)
        // Toast.makeText(this, "API: $OSNumber", Toast.LENGTH_SHORT).show()
        Log.d(DEBUG_TAG_ARC, "API: $OSNumber")

        //if OS is less than API 21
        if (OSNumber < Build.VERSION_CODES.LOLLIPOP) {
            val archType = Build.CPU_ABI2
            // Toast.makeText(this, archType, Toast.LENGTH_SHORT).show()
            val archType2 = Build.CPU_ABI2
            // Toast.makeText(this, archType2, Toast.LENGTH_SHORT).show()
            Log.d(DEBUG_TAG_ARC, "Supports the following: $archType and $archType2")

            //here is were you do something with the values
            //somthing like this
            //32BIT
            if (archType == "x86" || archType == "x86") {
                return true
            } else if (archType == "x86_64" || archType == "x86_64") {
                return false
            }
        }

        //if OS is greater than or equal toAPI 21
        //check for supported ABIS
        if (OSNumber >= Build.VERSION_CODES.LOLLIPOP) {
            val supportedABIS = Build.SUPPORTED_ABIS
            val supportedABIS_32_BIT = Build.SUPPORTED_32_BIT_ABIS
            val supportedABIS_64_BIT = Build.SUPPORTED_64_BIT_ABIS
            for (aSupportedABI in supportedABIS) {
                Log.d(DEBUG_TAG_ARC, aSupportedABI!!)
            }
            for (aSupportedABI in supportedABIS_32_BIT) {
                Log.d(DEBUG_TAG_ARC, "32BIT : $aSupportedABI")
            }
            for (aSupportedABI in supportedABIS_64_BIT) {
                Log.d(DEBUG_TAG_ARC, "64BIT : $aSupportedABI")
            }

            //checks that there is support for 32 or 64BIT
            Log.d(DEBUG_TAG_ARC, "length of 32BIT support: " + supportedABIS_32_BIT.size)
            Log.d(DEBUG_TAG_ARC, "length of 64BIT support: " + supportedABIS_64_BIT.size)

            //to do something with the 32BIT Architecture
            //if there is nothing (0) in the ordered list then it means that it does not support it
            if (supportedABIS_32_BIT.size > 0) {
                //do something with the 32BIT ARCH
                return true
            } else if (supportedABIS_64_BIT.size > 0) {
                //do something with the 64BIT ARCH
                return false
            }
        }
        throw RuntimeException("Unexpected result")
    }
}