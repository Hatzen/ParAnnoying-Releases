package de.hartz.software.parannoying.offline.model

import android.util.Log
import de.hartz.software.parannoying.core.interfaces.di.StorageInterface
import de.hartz.software.parannoying.core.model.persistence.realm.RealmHelper
import de.hartz.software.parannoying.offline.interfaces.repository.CrashlogRepository
import de.hartz.software.parannoying.offline.interfaces.repository.DeviceDataRepository
import de.hartz.software.parannoying.offline.interfaces.repository.DialogRepository
import de.hartz.software.parannoying.offline.interfaces.repository.EventRepository
import de.hartz.software.parannoying.offline.interfaces.repository.ForwardDatasetRepository
import de.hartz.software.parannoying.offline.interfaces.repository.MessageRepository
import de.hartz.software.parannoying.offline.model.domain.SearchResult
import de.hartz.software.parannoying.offline.model.domain.Type
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineHiddenSettings
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineSettings
import de.hartz.software.parannoying.offline.model.persistence.repository.CrashlogRepositoryImplRealm
import de.hartz.software.parannoying.offline.model.persistence.repository.DeviceDataRepositoryImplRealm
import de.hartz.software.parannoying.offline.model.persistence.repository.DialogRepositoryImplRealm
import de.hartz.software.parannoying.offline.model.persistence.repository.EventRepositoryImplRealm
import de.hartz.software.parannoying.offline.model.persistence.repository.ForwardDatasetRepositoryImplRealm
import de.hartz.software.parannoying.offline.model.persistence.repository.MessageRepositoryImplRealm
import me.xdrop.fuzzywuzzy.FuzzySearch


class OfflineStorage(override val realmHelper: RealmHelper)
    : StorageInterface<OfflineHiddenSettings, OfflineSettings>,
    CrashlogRepository by CrashlogRepositoryImplRealm(realmHelper),
    DeviceDataRepository by DeviceDataRepositoryImplRealm(realmHelper),
    DialogRepository by DialogRepositoryImplRealm(realmHelper),
    MessageRepository by MessageRepositoryImplRealm(realmHelper),
    EventRepository by EventRepositoryImplRealm(realmHelper),
    ForwardDatasetRepository by ForwardDatasetRepositoryImplRealm(realmHelper) {

    companion object {
        lateinit var INSTANCE: OfflineStorage
    }

    init {
        // TODO: Remove with dagger injection should work everywhere see NotificaationService for instance...
        INSTANCE = this

        Log.e(javaClass.simpleName, "Storage created " + realmHelper.FILE_NAME)
    }

    override fun isMigrationNeeded(): Boolean {
        return realmHelper.isMigrationNeeded()
    }

    override fun runMigration() {
        realmHelper.getThreadInstance().use {
            // Migration done
        }
    }

    @Deprecated("Remove usually we delete all data..")
    fun deleteRealm() {
        realmHelper.deleteRealmAndCloseCurrentSession()
    }

    // TODO: Currently this seem to provide duplicate entries..
    fun searchDialogsAndMessages(text: String): List<SearchResult> {
        val THRESHOLD = 70
        val MAX_PER_USER = 4

        val result = mutableListOf<SearchResult>()
        val potentialMatches = mutableListOf<Pair<Int, SearchResult>>()

        getDialogs().forEach { user ->
            val userPotentials = mutableListOf<Pair<Int, SearchResult>>()

            // Check name similarity
            val ratioNickname = FuzzySearch.partialRatio(text, user.nickname)
            val ratioOriginal = FuzzySearch.partialRatio(text, user.originalName)

            if (ratioNickname > THRESHOLD || ratioOriginal > THRESHOLD) {
                result.add(SearchResult(user.nickname, Type.USER, user))
            } else {
                val bestRatio = maxOf(ratioNickname, ratioOriginal)
                userPotentials.add(bestRatio to SearchResult(user.nickname, Type.USER, user))
            }

            // TODO: Maybe use chunking here as well? But we already need all..
            // Check messages similarity
            for (message in getAllMessagesForUser(user)) {
                val msgRatio = FuzzySearch.weightedRatio(text, message.text)
                if (msgRatio > THRESHOLD) {
                    result.add(SearchResult(message.text, Type.MESSAGE, user, message))
                } else {
                    userPotentials.add(msgRatio to SearchResult(message.text, Type.MESSAGE, user, message))
                }
            }

            // Keep only the best 3 for this user
            potentialMatches += userPotentials
                .sortedByDescending { it.first }
                // TODO: this is wrong we want to show 100% of 100% only below 70 we want only top 4
                .take(MAX_PER_USER)
        }

        if (result.size < 3) {
            potentialMatches
                .sortedByDescending { it.first }
                .take(10)
                .mapTo(result) { it.second }
        }

        return result
    }

}