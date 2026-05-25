package de.hartz.software.parannoying.offline.adapters.view.chat

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.developer.filepicker.model.DialogConfigs
import com.developer.filepicker.model.DialogProperties
import com.developer.filepicker.view.FilePickerDialog
import com.mikepenz.iconics.view.IconicsButton
import com.mikepenz.iconics.view.IconicsTextView
import com.stfalcon.chatkit.commons.ImageLoader
import com.stfalcon.chatkit.utils.DateFormatter
import de.hartz.software.parannoying.core.extensions.launchActivity
import de.hartz.software.parannoying.core.helper.io.IOHelper
import de.hartz.software.parannoying.core.helper.ui.IconHelper
import de.hartz.software.parannoying.core.helper.ui.getFileZipper
import de.hartz.software.parannoying.core.helper.ui.getPlayIcon
import de.hartz.software.parannoying.offline.R
import de.hartz.software.parannoying.offline.activities.offline.ChatActivity
import de.hartz.software.parannoying.offline.activities.offline.userinfo.UserInfoActivity
import de.hartz.software.parannoying.offline.model.domain.messages.AbstractMessage
import de.hartz.software.parannoying.offline.model.domain.messages.FileMessage
import java.io.File


object ChatAdapterHelper {

    private fun hideUserImage(itemView: View) {
        val avatar = itemView.findViewById<com.stfalcon.chatkit.utils.ShapeImageView>(R.id.dialogAvatar)
        val senderName = itemView.findViewById<TextView>(R.id.messageSender)
        avatar.visibility = View.GONE
        senderName.visibility = View.GONE
    }

    fun initOpenSimpleFile(itemView: View, message: FileMessage, payload: ChatViewHolderPayload) {
        val openFileButton = itemView.findViewById<IconicsButton>(R.id.open_file)

        openFileButton.setText(message.fileSizeText)
        openFileButton.iconicsDrawableStart = payload.context.getFileZipper(IconHelper.SMALL_ICON_WHITE)
        openFileButton.setOnClickListener {
            ChatAdapterHelper.openSimpleFile(payload, message)
        }
    }

    fun setUserImage(itemView: View, message: AbstractMessage, payload: ChatViewHolderPayload, imageLoader: ImageLoader) {
        val chatViewType = payload.chatViewType
        val context = payload.context
        val avatar = itemView.findViewById<com.stfalcon.chatkit.utils.ShapeImageView>(R.id.dialogAvatar)
        val senderName = itemView.findViewById<TextView>(R.id.messageSender)
        if (chatViewType == ChatAdapter.ChatViewType.SINGLE_USER) {
            hideUserImage(itemView)
        } else {
            avatar.visibility = View.VISIBLE
            senderName.visibility = View.VISIBLE
            // TODO: https://github.com/Hatzen/ParAnnoying/issues/207

            imageLoader.loadImage(avatar, message?.sender?.dialogPhoto ,null)
            val prefix = if (chatViewType == ChatAdapter.ChatViewType.OFFLINE_GROUP)  "MAYBE: " else ""
            senderName.text = prefix + message?.sender?.nickname
            avatar.setOnClickListener(object: View.OnClickListener {
                override fun onClick(v: View?) {
                    val passParams : (userIntent : Intent) -> Unit = {
                        it.putExtra(ChatActivity.EXTRA_USER, message!!.sender!!.getUniqueDialogId())
                    }
                    context.launchActivity<UserInfoActivity>(init = passParams)
                }
            })
        }
    }

    fun initMediaMessage(itemView: View, payload: ChatViewHolderPayload, message: FileMessage, imageLoader: ImageLoader) {
        val openImage = itemView.findViewById<ImageView>(R.id.image)
        imageLoader.loadImage(openImage, message.filePath,null)

        openImage.setOnClickListener {
            ChatAdapterHelper.openMediaFile(message, payload.context)
        }
    }

    fun addPlayOverlay(itemView: View, payload: ChatViewHolderPayload) {
        val context = payload.context
        val overlay = itemView.findViewById<TextView>(R.id.imageOverlay)
        overlay.text = ""
        overlay.background = context.getPlayIcon(IconHelper.LARGE_ICON_WHITE)
        overlay.visibility = View.VISIBLE
    }

    fun hideOverlay(parentView: View) {
        val imageOverlay = parentView.findViewById<TextView>(R.id.imageOverlay)
        imageOverlay.visibility = View.GONE
    }

    fun setOverlayForFiles(parentView: View, numberOfFiles: Int) {
        if (numberOfFiles > 1) {
            setOverlay(parentView, "$numberOfFiles further files")
        } else {
            hideOverlay(parentView)
        }
    }

    fun setOverlay(parentView: View, text: String) {
        val imageOverlay = parentView.findViewById<TextView>(R.id.imageOverlay)
        imageOverlay.visibility = View.VISIBLE
        imageOverlay.text = text
    }

    fun setTime(parentView: View, message: AbstractMessage) {
        val timeText = DateFormatter.format(message.getCreatedAt(), DateFormatter.Template.TIME)
        val messageTime = parentView.findViewById<TextView>(R.id.messageTime)
        messageTime.setText(timeText)
    }

    fun openSimpleFile(payload: Any, message: FileMessage) {
        val context = (payload as ChatViewHolderPayload).context
        try {
            // https://stackoverflow.com/a/38757416/8524651
            val file: File = File(message.filePath)
            val fileExtension = file.extension
            val path = Uri.fromFile(file)
            val fileIntent = Intent(Intent.ACTION_VIEW)
            fileIntent.setDataAndType(path, "application/$fileExtension")
            context.startActivity(fileIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "Cant find application for your File", Toast.LENGTH_LONG).show()

            // // TODO this is not really working..
            // // https://stackoverflow.com/a/18380079/8524651
            // // https://stackoverflow.com/a/22319727/8524651
            // val intent = Intent()
            // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // // val uri = Uri.parse(message.filePath.substringBeforeLast("/"))
            // // intent.setDataAndType(uri, "file/*")
            // //intent.setDataAndType(uri, "resource/folder")
            // intent.setType("file/*")
            // context.startActivity(intent)


            val properties = DialogProperties()
            properties.selection_mode = DialogConfigs.SINGLE_MODE
            properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT
            properties.root = File(message.filePath.substringBeforeLast("/"))
            properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
            val importDialog = FilePickerDialog(context, properties)
            importDialog.setTitle("Open this directory with an app able to work with the file:")
            importDialog.show()
        }
    }


    fun openMediaFile(message: FileMessage, context: Context) {

        val files = arrayListOf<String>()
        if (message.numberOfFiles == 1) {
            files.add(File(message.filePath).canonicalPath)
        } else {
            throw RuntimeException("get rid of zip and use extracted folders for performance reasons..")
        }

        val newFile = File(message.filePath)
        val uri = FileProvider.getUriForFile(context, IOHelper.getFileProviderPackagename(context), newFile)
        val viewIntent = Intent(Intent.ACTION_VIEW)

        viewIntent.type = context.getContentResolver().getType(uri)
        viewIntent.data = uri
        viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(viewIntent)
    }

    fun startInfiniteRotation(imageView: IconicsTextView, liveData: LiveData<Boolean>) {
        val animator = ObjectAnimator.ofFloat(imageView, View.ROTATION, 0f, 360f).apply {
            duration = 1000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
        }

        // Beobachte LiveData
        val observer = object : Observer<Boolean> {
            override fun onChanged(isLoading: Boolean) {
                if (isLoading == true) {
                    if (!animator.isRunning) animator.start()
                } else {
                    animator.cancel()
                    imageView.rotation = 0f // Optional: zurücksetzen
                }
            }
        }

        liveData.observeForever(observer)

        // Optional: Entferne Observer bei Recycling (z.B. im Adapter)
        imageView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {}
            override fun onViewDetachedFromWindow(v: View) {
                liveData.removeObserver(observer)
                animator.cancel()
            }
        })
    }
}
