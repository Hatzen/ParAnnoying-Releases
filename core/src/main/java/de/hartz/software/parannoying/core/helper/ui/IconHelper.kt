package de.hartz.software.parannoying.core.helper.ui

import android.content.Context
import android.graphics.drawable.Drawable
import com.mikepenz.iconics.Iconics
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.mikepenz.iconics.typeface.library.community.material.CommunityMaterial
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.typeface.library.materialdesigniconic.MaterialDesignIconic
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.paddingDp
import com.mikepenz.iconics.utils.sizeDp
import de.hartz.software.parannoying.core.R

const val smallIconDp = 50
const val smallPadding = 7
const val largeIconDp = 512
const val largePadding = 50

data class IconTemplate(val sizeDp: Int, val paddingDp: Int, val colorId: Int)

object IconHelper {
    // TODO: SmallIconDp for medium?
    val MEDIUM_ICON_WHITE = IconTemplate(smallIconDp,0, R.color.colorWhite)
    val SMALL_ICON_WHITE = IconTemplate(smallIconDp, smallPadding, R.color.colorWhite)
    val SMALL_ICON_ACCENT = IconTemplate(smallIconDp, smallPadding, R.color.colorAccent)
    val SMALL_ICON_PRIMARY = IconTemplate(smallIconDp, smallPadding, R.color.colorPrimaryDark)
    val LARGE_ICON_WHITE = IconTemplate(largeIconDp, largePadding, R.color.colorWhite)
    val LARGE_ICON_PRIMARY = IconTemplate(largeIconDp, largePadding, R.color.colorPrimaryDark)
    val LARGE_ICON_ACCENT = IconTemplate(largeIconDp, largePadding, R.color.colorAccent)

    fun initIcons(context: Context) {
        // TODO: It is strange we need to init this manually, that has to do with wrong packages and multimodule stuff..
        //   https://github.com/mikepenz/Android-Iconics/issues/498
        // Iconics.init(context, R.string::class.java.fields)
        // Iconics.init(context) // Will lead to error..

        Iconics.registerFont(FontAwesome)
        Iconics.registerFont(CommunityMaterial)
        Iconics.registerFont(MaterialDesignIconic)
    }
}

private fun getDrawable(context: Context, icon: IIcon, IconTemplate: IconTemplate): Drawable {
    val drawable = IconicsDrawable(context, icon)
    drawable.colorInt = context.resources.getColor(IconTemplate.colorId)
    drawable.sizeDp = IconTemplate.sizeDp
    drawable.paddingDp = IconTemplate.paddingDp
    return drawable
}

// TODO: Move to Context.IconHelper.getIcon
fun Context.getGearIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_cog, IconTemplate)
}

fun Context.getTrashIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_trash, IconTemplate)
}


fun Context.getSoundIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_phone_volume, IconTemplate)
}
fun Context.getSDCardIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_sd_card, IconTemplate)
}

fun Context.getVideoIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_film, IconTemplate)
}

fun Context.getFileZipper(IconTemplate: IconTemplate): IconicsDrawable {
    return getDrawable(this, FontAwesome.Icon.faw_file_archive, IconTemplate) as IconicsDrawable
}

fun Context.getPlayIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_play_circle, IconTemplate)
}

fun Context.getPlusIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_plus, IconTemplate)
}

fun Context.getForwardIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_share, IconTemplate)
}

fun Context.getNfcIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, MaterialDesignIconic.Icon.gmi_nfc, IconTemplate) // Nfc symbol from community wanted
}

fun Context.getBluetoothIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_bluetooth, IconTemplate)
}

fun Context.getBluetoothDisconnectedIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_bluetooth_off, IconTemplate)
}

fun Context.getBluetoothConnectingIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_bluetooth_connect, IconTemplate)
}

fun Context.getCameraIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_camera, IconTemplate)
}

fun Context.getTextIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_clipboard_text_multiple, IconTemplate)
}

// app:srcCompat="@mipmap/ic_group_add_white"
fun Context.getUserIdIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_account_circle, IconTemplate)
}

fun Context.getUserIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_account, IconTemplate)
}

fun Context.getGroupIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_users, IconTemplate)
}

fun Context.getAddIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_plus, IconTemplate)
}

fun Context.getServerIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_server, IconTemplate)
}

fun Context.getEventLogIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_list, IconTemplate)
}

fun Context.getSyncMessagesIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_sync_alt, IconTemplate)
}

fun Context.getPinIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_keyboard, IconTemplate)
}

fun Context.getFeedbackIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_comment_dots, IconTemplate)
}

fun Context.getDownloadApkIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_download, IconTemplate)
}

fun Context.getExportIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_file_export, IconTemplate)
}

fun Context.getAddUserIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_account_multiple_plus, IconTemplate)
}

fun Context.getAddMessageIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_android_messages, IconTemplate)
}

fun Context.getSendIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_paper_plane, IconTemplate)
}

fun Context.getShareIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_share_alt, IconTemplate)
}


fun Context.getYesIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_check, IconTemplate)
}

fun Context.getNoIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_times, IconTemplate)
}

fun Context.getKeyIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_account_key, IconTemplate)
}

fun Context.getBackupIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_history, IconTemplate)
}

fun Context.getInboxIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon2.cmd_inbox_arrow_down ,IconTemplate)
}

fun Context.getOutboxIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon2.cmd_inbox_arrow_up, IconTemplate)
}


fun Context.getFileIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_folder, IconTemplate)
}

fun Context.getPhoneIdIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_id_card, IconTemplate)
}

fun Context.getPhoneIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_phone, IconTemplate)
}

fun Context.getCardIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_cards, IconTemplate)
}

fun Context.getInfoIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_info, IconTemplate)
}

fun Context.getFileSendIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_cloud_upload, IconTemplate)
}

fun Context.getFileReceiveIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, CommunityMaterial.Icon.cmd_cloud_download, IconTemplate)
}

// Purpose
fun Context.getBugIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_bug, IconTemplate)
}

fun Context.getOnlineIdIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_passport, IconTemplate)
}

// Role

fun Context.getOfflineDeviceIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_phone_slash, IconTemplate)
}

fun Context.getOnlineDeviceIcon(IconTemplate: IconTemplate): Drawable {
    return getDrawable(this, FontAwesome.Icon.faw_phone, IconTemplate)
}