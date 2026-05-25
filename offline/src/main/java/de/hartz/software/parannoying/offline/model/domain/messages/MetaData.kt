package de.hartz.software.parannoying.offline.model.domain.messages

import android.content.Context
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.core.model.persistence.realm.UniqueRealmObject
import de.hartz.software.parannoying.offline.model.domain.UserSecurity
import de.hartz.software.parannoying.offline.model.domain.settings.OfflineSettings
import java.util.Scanner
import java.util.regex.Pattern

/**
 * Generic patterns like Json is not used to be more cryptic and reduce data.
 */
class MetaData {
    companion object {
        // TODO: With only group the meta data seems to contain the ; delimiter
        private val DELIMITER = "§"
    }

    var persistenceId: Long = UniqueRealmObject.ID_META_NEWEST_ID

    /**
     * Classifying the senders security.
     */
    var securityRisks: String = ""

    /**
     * The app version of the message sender for handling compatibility cases.
     * TODO: Android Version needed as well?
     */
    var appVersion: String = "-1"

    /**
     * Token that the receiver should use for encrypting next message. Set by sender
     */
    var newToken = ""

    /**
     * The token which was send before. Set by sender.
     */
    var previousToken = ""

    /**
     * A hash of all tokens since last received message
     */
    var tokenCheckSum= ""

    // TODO: use renewKeytoken to combine with current symmetric key and hash. Is renewing keys useful for symmetric as IV does the same doesnt it?
    /**
     * Flag (Or better Seed?) that indicates that the keys have to be renewed
     */
    var renewKey = ""

    /**
     * index of message when a single message is too long.
     */
    var sequenceNumber = -1
    /**
     * number of messages to concat multiple messages limited by decryption maximum chunk size.
     */
    var maxSequenceNumber = -1
    /**
     * uniqueId to identifiy messages which relate to each other and share the same sequenceNumbers.
     */
    var messageUuid = ""

    /**
     * TODO: Use when do not persist any message is set.
     */
    var deleteMessage = ""


    fun initWithoutTokens(context: Context,
                          applicationInfoComponent: ApplicationInfoComponent,
                          settings: OfflineSettings
    ) : MetaData {
        determineAppVersion(applicationInfoComponent)
        determineSecurity(context, settings)
        return this
    }

    fun init(context: Context,
             applicationInfoComponent: ApplicationInfoComponent,
             settings: OfflineSettings,
             tokens: MessageTokenWrapper
    ) : MetaData {
        determineAppVersion(applicationInfoComponent)
        determineSecurity(context, settings)
        newToken = tokens.newToken
        previousToken = tokens.previousToken
        tokenCheckSum = tokens.tokenCheckSum
        return this
    }

    fun isPartOfLargeMessage(): Boolean {
        return sequenceNumber != -1
    }

    override fun toString() : String {
        return listOf(
            appVersion,
            securityRisks,
            newToken,
            previousToken,
            tokenCheckSum,
            renewKey,
            messageUuid,
            sequenceNumber
        ).joinToString(DELIMITER, postfix = DELIMITER)
    }

    fun dataFromString (string: String) {
        val scanner = Scanner(string)
        scanner.useDelimiter(Pattern.compile(DELIMITER))
        appVersion = scanner.next()
        securityRisks = scanner.next()
        newToken = scanner.next()
        previousToken = scanner.next()
        tokenCheckSum = scanner.next()
        renewKey = scanner.next()
        messageUuid = scanner.next()
        sequenceNumber = scanner.nextInt()
    }

    private fun determineSecurity(context: Context, settings: OfflineSettings) {
        securityRisks = UserSecurity.getAllByContextAsString(context, settings)
    }

    private fun determineAppVersion(context: ApplicationInfoComponent) {
        appVersion = context.getVersion()
    }

}
