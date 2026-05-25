package de.hartz.software.parannoying.core.fragments.welcome

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.extensions.Storage
import de.hartz.software.parannoying.core.fragments.welcome.generic.WelcomeSimpleTextFragment
import de.hartz.software.parannoying.core.helper.ui.DialogHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getAddMessageIcon
import de.hartz.software.parannoying.core.helper.ui.getBluetoothIcon
import de.hartz.software.parannoying.core.helper.ui.getCameraIcon
import de.hartz.software.parannoying.core.helper.ui.getFileIcon
import de.hartz.software.parannoying.core.helper.ui.getPhoneIcon
import de.hartz.software.parannoying.core.helper.ui.getPhoneIdIcon
import de.hartz.software.parannoying.core.model.domain.DeviceRole
import de.hartz.software.parannoying.core.model.domain.settings.Channels


open class  WelcomePermissionsFragment : WelcomeBaseFragment() {

    companion object {
        val KEY_HEADER = "KEY_HEADER"
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    private var color: Int = 0
    private lateinit var heading: String

    private lateinit var cameraPermissionButton: AppCompatImageButton
    private lateinit var bluetoothPermissionButton: AppCompatImageButton
    private lateinit var phonePermissionButton: AppCompatImageButton
    private lateinit var storagePermissionButton: AppCompatImageButton
    private lateinit var permission_notification: AppCompatImageButton
    // Permission for detecting phone connection.
    private lateinit var locationPermissionButton: AppCompatImageButton

    lateinit var callback: () -> Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        heading = arguments?.getString(KEY_HEADER) ?: ""
        color = arguments?.getInt(WelcomeSimpleTextFragment.KEY_COLOR) ?: 0
        if (savedInstanceState != null) {
            //now do something with savedInstanceState
        }

    }

    override fun onResume() {
        super.onResume()
        updateButtons()
    }

    fun requestOptionalPermissions() {
        requestPermissionsLauncher.launch(
            getOptionalPermissions()
        )
    }

    // TODO: Currently not possible to request optional and required permissions at the same time
    fun getOptionalPermissions(): Array<String> {
        return arrayOf(
            // Manifest.permission.CAMERA,
            // Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            // Manifest.permission.READ_PHONE_STATE,
            // TODO: these are optional but should be requested..
            Manifest.permission.POST_NOTIFICATIONS,
            // (Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    fun getRequiredPermissions (): List<String> {
        return arrayOf(
                Manifest.permission.CAMERA.takeIf { !cameraOk() },
                Manifest.permission.ACCESS_FINE_LOCATION.takeIf { !bluetoothOk() },
                Manifest.permission.READ_PHONE_STATE.takeIf { !phoneIdOk() },
                (Manifest.permission.ACCESS_COARSE_LOCATION).takeIf { !phoneOk() }
                ).filterNotNull()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.welcome_page_permissions, container, false)
        setText(view.findViewById<TextView>(de.hartz.software.parannoying.core.R.id.welcomeHeading), heading)
        cameraPermissionButton = view.findViewById<AppCompatImageButton>(R.id.permission_camera)

        callback()

        cameraPermissionButton.setImageDrawable(requireActivity().getCameraIcon(IconHelper.SMALL_ICON_WHITE))
        cameraPermissionButton.setOnClickListener {
            DialogHelper.showDialog(requireContext(), "Camera Permission Info", "To use the camera channel for scanning qrcodes needed.")
        }

        bluetoothPermissionButton = view.findViewById<AppCompatImageButton>(R.id.permission_location)
        bluetoothPermissionButton.setImageDrawable(requireActivity().getBluetoothIcon(IconHelper.SMALL_ICON_WHITE))
        bluetoothPermissionButton.setOnClickListener {
            DialogHelper.showDialog(requireContext(), "Bluetooth Permission Info", "To use the bluetooth channel properly we need the permissions for location detection. As it is possible to determine relative location to other devices with exact location info.")
        }

        storagePermissionButton = view.findViewById<AppCompatImageButton>(R.id.permission_storage)
        storagePermissionButton.setImageDrawable(requireActivity().getFileIcon(IconHelper.SMALL_ICON_WHITE))
        storagePermissionButton.setOnClickListener {
            DialogHelper.showDialog(requireContext(), "Memory Permission Info", "Optional permission it is only needed to share qr codes or make backups.")
        }
        storagePermissionButton.background = requireActivity().resources.getDrawable(R.drawable.button_shape_disabled)

        phonePermissionButton = view.findViewById<AppCompatImageButton>(R.id.permission_phone)
        phonePermissionButton.setImageDrawable(requireActivity().getPhoneIdIcon(IconHelper.SMALL_ICON_WHITE))
        phonePermissionButton.setOnClickListener {
            DialogHelper.showDialog(requireContext(), "Phone Permission Info", "Needed to get your hardware address and check for cellular connections to deny silent sms and malicious mms.")
        }

        locationPermissionButton = view.findViewById<AppCompatImageButton>(R.id.permission_coarse_location)
        locationPermissionButton.setImageDrawable(requireActivity().getPhoneIcon(IconHelper.SMALL_ICON_WHITE))
        locationPermissionButton.setOnClickListener {
            DialogHelper.showDialog(requireContext(), "Coarse Location", "Needed to connect bluetooth devices, could be used to determine your relativ location to connection partner.")
        }

        permission_notification = view.findViewById<AppCompatImageButton>(R.id.permission_notification)
        permission_notification.setImageDrawable(requireActivity().getAddMessageIcon(IconHelper.SMALL_ICON_WHITE))
        permission_notification.setOnClickListener {
            DialogHelper.showDialog(requireContext(), "Notifications", "Allow showing notifications otherwise background tasks might not work properly.")
        }

        updateButtons()
        view.invalidate()
        return view
    }

    override fun canMoveFurther(): Boolean {
        updateButtons()

        return true
    }

    override fun cantMoveFurtherErrorMessage(): String {
        if (!phoneIdOk()) {
            return "Phone permissions required"
        } else if (!cameraOk()) {
            return "Camera permissions required"
        } else if (!bluetoothOk()) {
            return "Bluetooth permissions required"
        } else if (!phoneOk()) {
            return "Cellular phone permissions required"
        }
        throw RuntimeException("move further checks only these 3")
    }

    fun updateButtons () {
        if (!cameraOk()) {
            cameraPermissionButton.background = requireActivity().resources.getDrawable(R.drawable.button_shape)
        } else {
            cameraPermissionButton.background = requireActivity().resources.getDrawable(R.drawable.button_shape_disabled)
        }
        if (!phoneIdOk()) {
            phonePermissionButton.background = requireActivity().resources.getDrawable(R.drawable.button_shape)
        } else {
            phonePermissionButton.background = requireActivity().resources.getDrawable(R.drawable.button_shape_disabled)
        }
        if (!bluetoothOk()) {
            bluetoothPermissionButton.background = requireActivity().resources.getDrawable(R.drawable.button_shape)
        } else {
            bluetoothPermissionButton.background = requireActivity().resources.getDrawable(R.drawable.button_shape_disabled)
        }
        if (!phoneOk()) {
            locationPermissionButton.background = requireActivity().resources.getDrawable(R.drawable.button_shape)
        } else {
            locationPermissionButton.background = requireActivity().resources.getDrawable(R.drawable.button_shape_disabled)
        }
        if (!notificationsOk()) {
            permission_notification.background = requireActivity().resources.getDrawable(R.drawable.button_shape)
        } else {
            permission_notification.background = requireActivity().resources.getDrawable(R.drawable.button_shape_disabled)
        }
        view?.invalidate()
    }

    fun phoneIdOk(): Boolean {
        return ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    fun cameraOk(): Boolean {
        return (!Storage.readSettings().allowedChannels.contains(Channels.CAMERA) || ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }

    fun bluetoothOk(): Boolean {
        return (!Storage.readSettings().allowedChannels.contains(Channels.BLUETOOTH) || ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    fun phoneOk(): Boolean {
        // TODO: can we compare it like this? Do we need to compare the deviceRole.IDs
        return (!Storage.deviceRole.equals(DeviceRole.OFFLINE) || ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
    }

    fun notificationsOk(): Boolean {
        return ActivityCompat.checkSelfPermission(requireActivity(), android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    override fun backgroundColor(): Int {
        if (color == 0) {
            return super.backgroundColor()
        }
        return color
    }

}