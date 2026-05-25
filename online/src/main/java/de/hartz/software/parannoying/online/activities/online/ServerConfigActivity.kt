package de.hartz.software.parannoying.online.activities.online

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getInboxIcon
import de.hartz.software.parannoying.core.helper.ui.getSendIcon
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.core.interfaces.di.security.DataConverter
import de.hartz.software.parannoying.online.R
import de.hartz.software.parannoying.online.databinding.ActivityServerConfigBinding
import de.hartz.software.parannoying.online.interfaces.OnlineApplication
import de.hartz.software.parannoying.online.model.OnlineStorage
import de.hartz.software.parannoying.online.model.domain.ServerConfig
import de.hartz.software.parannoying.online.model.domain.ServerType
import de.hartz.software.parannoying.online.viewmodel.ServerConfigViewModel
import javax.inject.Inject


class ServerConfigActivity : AppCompatActivity() {

    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    @Inject
    lateinit var dataConverter: DataConverter

    val onlineStorage get() = Storage as OnlineStorage

    private lateinit var binding: ActivityServerConfigBinding
    private val viewModel: ServerConfigViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityServerConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.save.setOnClickListener {
            val serverConfig = getConfigFromViewModel()
            save(serverConfig)
        }

        binding.inserProdConfig.visibility = if (onlineStorage.DEVELOPER_MODE) View.VISIBLE else View.GONE
        binding.inserProdConfig.setOnClickListener {
            val config = ServerConfig().apply {
                serverType = ServerType.FIREBASE
                name = "Dev to Prod switch"
                apiKey = "AIzaSyAa2t7FLluoylQ3tq_-PlOz-lvt9C8cw3E"
                appId = "1:135788970944:android:980386c393128795"
                projectId = "parannoying-bc93d"
                databaseUrl = "https://parannoying-bc93d.firebaseio.com"
                // TODO: is this used and properly extracted?
                gcmSenderId = "135788970944"
            }
            setViewModel(config)
        }

        (app as OnlineApplication).onlineComponents.inject(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = "Server Config"

        // Setup spinner with enum values
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item,
            ServerType.SERVER_TYPE_LIST.map { it.getName(this) })
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerServerType.adapter = adapter

        binding.spinnerServerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.serverType.value = ServerType.SERVER_TYPE_LIST[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val settings = onlineStorage.readSettings()
        setViewModel(settings.serverConfigs.firstOrNull())

        bindEditText(binding.editName, viewModel.name)
        bindEditText(binding.editApiKey, viewModel.apiKey)
        bindEditText(binding.editAppId, viewModel.appId)
        bindEditText(binding.editDatabaseUrl, viewModel.databaseUrl)
        bindEditText(binding.editProjectId, viewModel.projectId)
        bindEditText(binding.editGcmSenderId, viewModel.gcmSenderId)
    }

    private fun bindEditText(editText: EditText, liveData: MutableLiveData<String>) {
        editText.addTextChangedListener {
            liveData.value = it?.toString()
        }
        liveData.observe(this) {
            if (editText.text.toString() != it) {
                editText.setText(it)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.server_config_menu, menu)
        return true
    }
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.action_send)?.icon = getSendIcon(IconHelper.SMALL_ICON_WHITE)
        menu?.findItem(R.id.action_receive)?.icon = getInboxIcon(IconHelper.SMALL_ICON_WHITE)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_send) {
            val data = onlineStorage.readSettings().serverConfigs
            val result = dataConverter.objectToString(data)
            airGapAdapter.startSend(UseCases.Online.SERVER_CONFIG_SEND.useText(result))
        } else if (item.itemId == R.id.action_receive) {
            airGapAdapter.startReceive(UseCases.Online.SERVER_CONFIG_RECEIVE)
        }
        if (item.itemId == android.R.id.home) {
            super.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = airGapAdapter.onActivityResult(requestCode, resultCode, data)
        if (UseCases.Online.SERVER_CONFIG_SEND == result.getUseCase()) {
            val jsonServerConfig = result.getSingleResult().exchangeData
            val result = dataConverter.stringToObject(jsonServerConfig, ServerConfig::class.java)
            setViewModel(result)
        }
    }

    private fun setViewModel(config: ServerConfig?) {
        if (config == null) {
            return
        }
        viewModel.serverType.value = config.serverType
        viewModel.name.value = config.name
        viewModel.apiKey.value = config.apiKey
        viewModel.appId.value = config.appId
        viewModel.projectId.value = config.projectId
        viewModel.databaseUrl.value = config.databaseUrl
        viewModel.gcmSenderId.value = config.gcmSenderId
    }

    private fun getConfigFromViewModel(): ServerConfig {
        val config = ServerConfig().apply {
            serverType = viewModel.serverType.value ?: error("ServerType is null")
            name = viewModel.name.value ?: "Default Name"
            apiKey = viewModel.apiKey.value ?: ""
            appId = viewModel.appId.value ?: ""
            projectId = viewModel.projectId.value ?: ""
            databaseUrl = viewModel.databaseUrl.value ?: ""
            gcmSenderId = viewModel.gcmSenderId.value ?: ""
        }
        return config
    }

    private fun save(config: ServerConfig) {
        if (config.serverType == ServerType.SUPABASE) {
            DialogHelper.showAlert(this, "Supabase is not yet supported, config not stored.")
            return
        }
        onlineStorage.updateSettings {
            it.serverConfigs = mutableListOf(config)
        }
    }

}
