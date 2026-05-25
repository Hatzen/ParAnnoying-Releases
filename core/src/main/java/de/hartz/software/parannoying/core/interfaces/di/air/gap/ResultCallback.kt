package de.hartz.software.parannoying.core.interfaces.di.air.gap
interface ResultCallback {

    fun onSuccess(resultData: String)
    fun onError(errorMsg: String, exception: Exception)

}