package de.hartz.software.parannoying.online.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import de.hartz.software.parannoying.online.model.domain.ServerType

class ServerConfigViewModel : ViewModel() {
    val serverType = MutableLiveData(ServerType.FIREBASE)
    val name = MutableLiveData("")
    val apiKey = MutableLiveData("")
    val appId = MutableLiveData("")
    val databaseUrl = MutableLiveData("")
    val projectId = MutableLiveData("")
    val gcmSenderId = MutableLiveData("")
}