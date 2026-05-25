package de.hartz.software.parannoying.offline.activities.offline

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.messages.MessageInput
import com.stfalcon.chatkit.messages.MessagesListAdapter
import com.stfalcon.chatkit.utils.ShapeImageView
import de.hartz.software.parannoying.air.gap.model.UseCases
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.io.ZipUtil
import de.hartz.software.parannoying.core.helper.security.DataGeneratorHelper
import de.hartz.software.parannoying.core.helper.security.DataSecurityHelper
import de.hartz.software.parannoying.core.helper.security.provider.SecurityInterfaceHolder
import de.hartz.software.parannoying.core.helper.ui.UiHelper.setRoundedBackground
import de.hartz.software.parannoying.core.interfaces.di.ApplicationInfoComponent
import de.hartz.software.parannoying.core.interfaces.di.air.gap.AirGapAdapter
import de.hartz.software.parannoying.offline.activities.offline.userinfo.UserInfoActivity
import de.hartz.software.parannoying.offline.adapters.view.chat.ChatAdapter
import de.hartz.software.parannoying.offline.databinding.ActivityChatsBinding
import de.hartz.software.parannoying.offline.helper.CleartextMessageProcessingWorker
import de.hartz.software.parannoying.offline.helper.onboarding.OfflineIntroducer
import de.hartz.software.parannoying.offline.model.domain.dialogs.BaseDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.OfflineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.OnlineGroup
import de.hartz.software.parannoying.offline.model.domain.dialogs.SimpleDialog
import de.hartz.software.parannoying.offline.model.domain.dialogs.UniqueDialogIdWrapper
import de.hartz.software.parannoying.offline.model.domain.dialogs.UnknownUser
import de.hartz.software.parannoying.offline.model.domain.dialogs.User
import de.hartz.software.parannoying.offline.model.domain.events.BaseEvent
import de.hartz.software.parannoying.offline.model.domain.events.MessageEvent
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage
import de.hartz.software.parannoying.offline.model.domain.messages.MetaData
import de.hartz.software.parannoying.offline.model.domain.messages.UserMessage
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject


class ChatActivity : BaseOfflineActivity(), MessagesListAdapter.SelectionListener,
        MessagesListAdapter.OnLoadMoreListener, MessageInput.InputListener {
    companion object {
        const val EXTRA_USER: String = "EXTRA_USER"
        const val EXTRA_MESSAGE: String = "EXTRA_MESSAGE"
        private const val UNINITIALIZED_LAST_LOADED_ID: Long = -1
        private const val MESSAGES_LOADED_PER_INTERVAL = 30
    }

    // Recyclerview should be on the last message for the very first chunk that got loaded.
    private var shouldScrollToBottom = true
    private val DONT_SCROLL_TO_SPECIFIC_MESSAGE = Long.MIN_VALUE
    private var scrollToSpecificMessageWithPersistenceId = DONT_SCROLL_TO_SPECIFIC_MESSAGE

    private val senderId get() = currentUser.persistenceId.toString()
    private var messagesAdapter: ChatAdapter? = null

    private var menu: Menu? = null
    private var selectionCount: Int = 0
    private var lastLoadedId: Long = UNINITIALIZED_LAST_LOADED_ID

    private val user: BaseDialog get() = passedUsersId.getDialog(Storage)
    private lateinit var passedUsersId: UniqueDialogIdWrapper

    private lateinit var binding: ActivityChatsBinding
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>

    @Inject
    lateinit var applicationInfoComponent: ApplicationInfoComponent
    @Inject
    lateinit var airGapAdapter: AirGapAdapter
    @Inject
    lateinit var securityInterfaceHolder: SecurityInterfaceHolder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        app.offlineComponents.inject(this)

        passedUsersId = intent.getSerializableExtra(EXTRA_USER) as UniqueDialogIdWrapper

        val passedTargetMessageId = intent.getLongExtra(EXTRA_MESSAGE, DONT_SCROLL_TO_SPECIFIC_MESSAGE)
        scrollToSpecificMessageWithPersistenceId = passedTargetMessageId

        initAdapter()

        initMessageCounterAndAttachments()

        val showUserInfo = object: View.OnClickListener {
            override fun onClick(v: View?) {
                val passParams : (userIntent : Intent) -> Unit = {
                    it.putExtra(EXTRA_USER, user.getUniqueDialogId())
                }
                launchActivity<UserInfoActivity>(init = passParams)
            }
        }

        // Setup title bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // show back button
        supportActionBar?.title = user.nickname
        // TODO: Cannot set onclick on title or actionbar directly..
        // supportActionBar?.setOnClickListener(showUserInfo)
        val avatar = findViewById<ShapeImageView>(de.hartz.software.parannoying.offline.R.id.profile_picture)
        avatar.setImageBitmap(IOHelper.getProfilePictureForUser(user.dialogPhoto, this))
        avatar.setOnClickListener(showUserInfo)

        // TODO: Move centralized somewhere and allow folder selection
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val uris = mutableListOf<Uri>()
                data?.data?.let { uris.add(it) }
                data?.clipData?.let { clipData ->
                    for (i in 0 until clipData.itemCount) {
                        uris.add(clipData.getItemAt(i).uri)
                    }
                }

                if (uris.isEmpty()) {
                    // Canceld.
                } else {
                    val tempFile = File.createTempFile("packed-send-files", ".zip", cacheDir)
                    val taretUri = Uri.fromFile(tempFile)  // Note: not recommended for sharing outside your app
                    ZipUtil().createZipFromUris(this, uris, taretUri) // CleartextMessageProcessingWorker
                    val file = uriToFile(this, taretUri)
                    if (file != null) {
                        CleartextMessageProcessingWorker
                            .enqueueCreatedFileMessage(this, file.absolutePath, user, applicationInfoComponent, this::callbackFileMessagesProcessed)
                    }
                }
            }
        }

        // Start onboarding if needed.
        OfflineIntroducer(this).startIntroduction()
    }

    private fun initMessageCounterAndAttachments() {
        val messageCountTextView = findViewById<TextView>(de.hartz.software.parannoying.offline.R.id.messageCount)
        setRoundedBackground(this@ChatActivity, messageCountTextView)
        messageCountTextView.visibility = View.GONE

        val input = findViewById<MessageInput>(de.hartz.software.parannoying.offline.R.id.input)
        input.setInputListener(this)
        input.inputEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable) {

                val numberOfMessages = editable.length / DataSecurityHelper.MAX_MESSAGE_SIZE + 1
                if (numberOfMessages > 1) {
                    messageCountTextView.visibility = View.VISIBLE
                    messageCountTextView.text = "$numberOfMessages messages"
                } else {
                    messageCountTextView.visibility = View.GONE
                }
            }
            override fun onTextChanged(s: CharSequence, st: Int, b: Int, c: Int) {}
            override fun beforeTextChanged(s: CharSequence, st: Int, c: Int, a: Int) {}
        })

        // Handle files
        input.setAttachmentsListener {
            startFilePicker()
        }
    }

    fun uriToFile(context: Context, uri: Uri): File? {
        return when (uri.scheme) {
            "file" -> File(uri.path ?: return null) // Direkt zugreifbar
            "content" -> {
                // Datei aus content:// kopieren
                val fileName = queryFileName(context, uri) ?: "temp_file"
                val tempFile = File.createTempFile(fileName, null, context.cacheDir)
                tempFile.outputStream().use { outputStream ->
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        copyStream(inputStream, outputStream)
                    } ?: return null
                }
                tempFile
            }
            else -> null
        }
    }

    private fun queryFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    private fun copyStream(input: InputStream, output: FileOutputStream) {
        val buffer = ByteArray(4 * 1024)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            output.write(buffer, 0, read)
        }
        output.flush()
    }

    private fun startFilePicker() {
        // Startverzeichnis (z.B. Downloads) angeben (optional, nicht garantiert)
        val downloadsUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download")

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // Alle Dateitypen
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Mehrfachauswahl erlauben
            putExtra(DocumentsContract.EXTRA_INITIAL_URI, downloadsUri) // Startverzeichnis
        }

        // Intent an Launcher übergeben
        filePickerLauncher.launch(intent)
    }

    override fun onSupportNavigateUp() : Boolean {
        // Enable Back button.
        finish()
        return true
    }

    override fun onPause() {
        super.onPause()
        val unread = Storage.getCountUnreadMessagesForUser(user)
        user.updateUnreadMessages(unread)
        this.Storage.updateDialog(user)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        this.menu = menu
        menuInflater.inflate(de.hartz.software.parannoying.offline.R.menu.chat_actions_menu, menu)
        menu.findItem(de.hartz.software.parannoying.offline.R.id.action_group_id)?.isVisible =
                user is OfflineGroup || user is OnlineGroup
        onSelectionChanged(0)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            de.hartz.software.parannoying.offline.R.id.action_delete -> {
                val selectedMessages = messagesAdapter!!.getSelectedMessages()!!.toList()
                // Update last loaded message.
                lastLoadedId = getLastLoadedIdForNotDeletedMessage(lastLoadedId, selectedMessages)

                Storage.removeMessages(selectedMessages)
                selectedMessages.forEach {
                    Storage.persistEvent(MessageEvent(BaseEvent.EVENT_DELETED_MESSAGE, it))
                }
                messagesAdapter!!.deleteSelectedMessages()
            }
            de.hartz.software.parannoying.offline.R.id.action_copy -> {
                messagesAdapter?.copySelectedMessagesText(this, getMessageStringFormatter(), true)
            }
            de.hartz.software.parannoying.offline.R.id.action_group_id -> {
                val groupId: String
                if (user is OfflineGroup) {
                    groupId = (user as OfflineGroup).groupId
                } else if (user is OnlineGroup) {
                    groupId = (user as OnlineGroup).groupId
                } else {
                    throw RuntimeException("User has not a groupId.")
                }
                airGapAdapter.startSend(UseCases.Offline.OFFLINE_GROUP_SEND.useText(groupId))
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getLastLoadedIdForNotDeletedMessage(currentLastLoadedId: Long, messagesToDelete: List<AbstractMessage>) : Long {
        val indexOfLastLoadedMessageWithinDeletedMessages = messagesToDelete.indexOfFirst {
            it.persistenceId == currentLastLoadedId
        }
        // Message with currentLastLoadedId wont be deleted.
        if (indexOfLastLoadedMessageWithinDeletedMessages == -1) {
            return currentLastLoadedId
        }
        val messages = mutableListOf<AbstractMessage>()

        // TODO: Load chunked..
        // Storage.getMessageChunkForUser(user, 0)
        val allMessages = Storage.getAllMessagesForUser(user)

        messages.addAll(allMessages)
        val indexOfLastLoadedMessage = messages.indexOfFirst {
            it.persistenceId == currentLastLoadedId
        }
        // If last message also got deleted reset id to uninitialized value.
        if (indexOfLastLoadedMessage == 0) {
            return UNINITIALIZED_LAST_LOADED_ID
        }
        val potentialNewLastLoadedMessageId = indexOfLastLoadedMessage - 1
        return getLastLoadedIdForNotDeletedMessage(messages[potentialNewLastLoadedMessageId].persistenceId, messagesToDelete)
    }

    override fun onBackPressed() {
        if (selectionCount == 0) {
            super.onBackPressed()
        } else {
            messagesAdapter?.unselectAllItems()
        }
    }

    override fun onLoadMore(page: Int, totalItemsCount: Int) {
        // TODO: Load chunked..
        // Storage.getMessageChunkForUser(user, 0)
        val allMessages = Storage.getAllMessagesForUser(user)
        if (totalItemsCount <= allMessages.size) {
            loadMessages()
        }
    }

    override fun onSelectionChanged(count: Int) {
        this.selectionCount = count
        menu?.findItem(de.hartz.software.parannoying.offline.R.id.action_delete)?.isVisible = count > 0
        menu?.findItem(de.hartz.software.parannoying.offline.R.id.action_copy)?.isVisible = count > 0
    }

    override fun onSubmit(input: CharSequence): Boolean {
        if (getText().isEmpty()) {
            return false
        }
        submit()
        return false
    }

    private fun submit() {
        CleartextMessageProcessingWorker.enqueueCreatedMessage(
            applicationContext, getText(), user, applicationInfoComponent, this::callbackMessagesProcessed)
        binding.input.getInputEditText().setText("")
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = airGapAdapter.onActivityResult(requestCode, resultCode, data)

        if (result.success == false) {
            // TODO: No sync and send activity cancled, so remove message?
        }
    }

    fun callbackFileMessagesProcessed(message: FileMessage) {
        runOnUiThread {
            messagesAdapter?.addToStart(message, true)
        }
    }


    fun callbackMessagesProcessed(message: UserMessage) {
        runOnUiThread {
            val newestPersistenceId = message.persistenceId
            // Prevent sent message to be loaded again if it is the only message yet, because it already was appended.
            if (lastLoadedId == ChatActivity.UNINITIALIZED_LAST_LOADED_ID) {
                lastLoadedId = newestPersistenceId
            }
            messagesAdapter?.addToStart(message, true)

            addTestAnswerMessage(message.relatedDialog!!, message)
        }
    }


    @Deprecated("Only testcode..")
    private fun addTestAnswerMessage(targetUser: BaseDialog, messageSent: UserMessage) {
        if (Storage.DEVELOPER_MODE && messageSent.message == "Test") {
            val messageReceived = UserMessage()
            messageReceived.message = "Response to Test"

            messageReceived.relatedDialog = targetUser
            messageReceived.createdAtTimestamp = IOHelper.getCurrentDateAsUnixTimestamp()
            if (targetUser is OnlineGroup) {
                messageReceived.sender = currentUser
                messageReceived.metaData = MetaData().initWithoutTokens(
                    this,
                    applicationInfoComponent,
                    Storage.readSettings()
                )
            } else if (targetUser is SimpleDialog) {
                if (targetUser is OfflineGroup) {
                    val unkown = UnknownUser(DataGeneratorHelper(securityInterfaceHolder).createFakeOnlineId())
                    unkown.initDummy()
                    messageReceived.sender = unkown
                } else if (targetUser is User) {
                    messageReceived.sender = targetUser
                }
                val messages = Storage.getAllMessagesForUser(targetUser)

                // TODO: This might change tokens, but we create a fake foreign message which should not manipulate tokens of user and just provide one.
                //   maybe just remove test code as it may break the token system.
                // val tokens = TokenDeterminer(securityInterfaceHolder).getTokensForMessageMetaData(targetUser, messages)

                messageReceived.metaData = MetaData().initWithoutTokens(
                    this,
                    applicationInfoComponent,
                    Storage.readSettings()
                    // tokens
                )
            }
            Storage.addMessage(messageReceived)
            messagesAdapter?.addToStart(messageReceived, true)
        }
    }

    @Synchronized
    private fun loadMessages() {
        val messages = mutableListOf<AbstractMessage>()
        // TODO: Load chunked..
        // Storage.getMessageChunkForUser(user, 0)
        val allMessages = Storage.getAllMessagesForUser(user)
        messages.addAll(allMessages)

        var actualCountOfLoadableMessages = MESSAGES_LOADED_PER_INTERVAL
        // Last message loaded on first time is -1 as the next message to load has index 0.
        val indexOfLastLoadedMessage = messages.indexOfFirst {
            it.persistenceId == lastLoadedId // UNINITIALIZED_LAST_LOADED_ID == lastLoadedId should never match and lead to indexOfLastLoadedMessage = -1!
        }
        val indexOfNextMessageToLoad = indexOfLastLoadedMessage + 1
        // Handle there might not be less messages than MESSAGES_LOADED_PER_INTERVAL
        val sizeOfMessagesAvailable = messages.size - indexOfNextMessageToLoad
        if (sizeOfMessagesAvailable < actualCountOfLoadableMessages) {
            actualCountOfLoadableMessages = sizeOfMessagesAvailable
        }
        val firstLoadedMessageIndex = indexOfNextMessageToLoad
        val lastLoadedMessageIndex = indexOfNextMessageToLoad + actualCountOfLoadableMessages
        val messagesToAppend = messages.subList(firstLoadedMessageIndex, lastLoadedMessageIndex)
        if (messagesToAppend.isEmpty()) {
            return
        }
        lastLoadedId = messagesToAppend.last().persistenceId

        // Add messages and scroll to last one after measurement is done.
        // https://stackoverflow.com/questions/42944005/recyclerview-cannot-call-this-method-in-a-scroll-callback/42944450
        binding.messagesList.post{
            messagesAdapter!!.addToEnd(messagesToAppend, false)
            if (shouldScrollToBottom) {
                val shouldScrollToSpecificMessage = scrollToSpecificMessageWithPersistenceId != DONT_SCROLL_TO_SPECIFIC_MESSAGE
                if (!shouldScrollToSpecificMessage) {
                    scrollToFirstNewMessage(messagesToAppend)
                } else {
                    // val dummyPos = 1 // messagesAdapter!!.getItems().size
                    // val targetMesssage = messagesAdapter!!.getItems().find { it.persistenceId == scrollToSpecificMessageWithPersistenceId }!!
                    scrollToSpecificMessage(scrollToSpecificMessageWithPersistenceId)
                }
            }
            // TODO: Collect all Ids and only set messageRead and save onStop activity.. As currently it is not displayed usually..
            // all messages which got loaded get marked as read. Would be nicer on render but it is the same as it is handled by the chatkit.
            for (message in messagesToAppend) {
                message.messageRead = true
            }
        }
    }

    private fun scrollToSpecificMessage(messagePersistenceId: Long) {
        // TODO: Load chunked..
        // Storage.getMessageChunkForUser(user, 0)
        val allMessages = Storage.getAllMessagesForUser(user)
        val newestMessage = allMessages.find { it.persistenceId == messagePersistenceId }
        if (newestMessage == null) {
            shouldScrollToBottom = false
            Log.e(javaClass.simpleName, "Cannot find message within chat for some reason.")
            return
        }

        val needsMoreMessages = !messagesAdapter!!.getItems().contains(newestMessage)
        if (!needsMoreMessages) {
            shouldScrollToBottom = false
            scrollToMessage(newestMessage)
        } else {
            // loadMessages()
            binding.messagesList.scrollToPosition(messagesAdapter!!.getItems().size - 1)
            Handler().postDelayed({
                scrollToSpecificMessage(messagePersistenceId)
            }, 1000)
        }
    }

    private fun scrollToFirstNewMessage(messagesToAppend: List<AbstractMessage>) {
        val newestMessage = messagesToAppend.find { !it.messageRead }

        val needsMoreMessages = newestMessage == messagesToAppend.first()
        if (!needsMoreMessages) {
            shouldScrollToBottom = false
            if (newestMessage != null) {
                scrollToMessage(newestMessage)
            } else {
                binding.messagesList.scrollToPosition(0)
            }
        } else {
            scrollToMessage(messagesToAppend.first())
        }
    }

    private fun scrollToMessage(message: AbstractMessage) {
        val position = messagesAdapter!!.getItems().indexOf(message)
        binding.messagesList.scrollToPosition(position)
    }

    private fun getMessageStringFormatter(): MessagesListAdapter.Formatter<AbstractMessage> {
        return MessagesListAdapter.Formatter { message ->
            val createdAt = SimpleDateFormat("MMM d, EEE 'at' h:mm a", Locale.getDefault())
                    .format(message.getCreatedAt())

            var text = message.getText()
            // TODO: This can be removed?
            if (message is FileMessage) text = "[attachment]" + text

            String.format(Locale.getDefault(), "%s: %s (%s)",
                    message.getUser().getName(), text, createdAt)
        }
    }

    private fun initAdapter() {
        val chatType = when (user) {
            is OfflineGroup -> {
                ChatAdapter.ChatViewType.OFFLINE_GROUP
            }
            is User -> {
                ChatAdapter.ChatViewType.SINGLE_USER
            }
            else -> {
                ChatAdapter.ChatViewType.ONLINE_GROUP
            }
        }
        val imageLoader = ImageLoader { imageView, avatarImageKey, payload ->
            val file = File(avatarImageKey!!)
            if (file.exists()) {
                Glide.with(this)
                        .load(file)
                        .into(imageView)

                val drawable = imageView.getDrawable()
                if (drawable is GifDrawable) {
                    drawable.stop()
                    drawable.startFromFirstFrame()
                    drawable.setLoopCount(GifDrawable.LOOP_FOREVER)
                }

            } else {
                val image = IOHelper.getProfilePictureForUser(avatarImageKey, this)
                imageView.setImageBitmap(image)
            }
        }
        messagesAdapter = ChatAdapter(senderId, chatType, this, imageLoader)
        messagesAdapter!!.enableSelectionMode(this)
        messagesAdapter!!.setLoadMoreListener(this)
        // TODO: R.id.bubble would catch a click on everything but it denies selecting anything
        messagesAdapter!!.registerViewClickListener(de.hartz.software.parannoying.offline.R.id.messageText) { view, message ->
            // Display cleartext when clicked if it is stared out by options
            val view = view.findViewById<TextView>(de.hartz.software.parannoying.offline.R.id.messageText)
            view.text = if(message is UserMessage) message.message else ""
        }
        binding.messagesList.setAdapter(messagesAdapter)

        messagesAdapter!!.notifyDataSetChanged()

        // https://github.com/stfalcon-studio/ChatKit/issues/171#issuecomment-405261680

        loadMessages()
        binding.messagesList.invalidate()
    }

    private fun getText() : String {
        return binding.input.getInputEditText().text.toString()
    }
}