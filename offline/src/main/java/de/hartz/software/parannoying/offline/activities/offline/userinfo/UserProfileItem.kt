package de.hartz.software.parannoying.offline.activities.offline.userinfo

import android.graphics.drawable.Drawable

sealed class UserProfileItem {
    data class ProfileImage(val seed: String) : UserProfileItem()
    data class SectionHeader(val title: String) : UserProfileItem()
    data class StaticInfo(val label: String?, val value: String) : UserProfileItem()
    data class Action(val text: String,
        val action: () -> Unit,
        val icon: Drawable? = null,
        val description: String? = null): UserProfileItem()
}