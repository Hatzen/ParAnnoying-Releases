package de.hartz.software.parannoying.core.helper.ui

import android.app.ActionBar
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.developer.filepicker.model.DialogConfigs
import com.developer.filepicker.model.DialogProperties
import com.developer.filepicker.view.FilePickerDialog
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.helper.io.FileHelper
import java.io.File

object DialogHelper {

    val INPUT_DIALOG_ID = "DIALOG_HELPER_INPUT_TAG"

    @Deprecated("Seems to be not working on Android S anymore..")
    fun showFileDialog(context: Context, callback: ((File) -> Unit)? = null) {
        val properties = DialogProperties()
        properties.selection_mode = DialogConfigs.MULTI_MODE
        properties.selection_type = DialogConfigs.FILE_AND_DIR_SELECT
        properties.root = File(DialogConfigs.DEFAULT_DIR)
        properties.error_dir = File(DialogConfigs.DEFAULT_DIR)
        properties.offset = File(DialogConfigs.DEFAULT_DIR)
        properties.extensions = null
        val importDialog = FilePickerDialog(context, properties)
        importDialog.setTitle("Select files or directories to send")
        importDialog.setDialogSelectionListener {
            if (it.isEmpty()) {
                return@setDialogSelectionListener
            }

            var file = File(it.get(0))
            if (it.size > 1) {
                val fileName = file.nameWithoutExtension
                file = FileHelper.zip(it, fileName)
            }

            callback?.invoke(file)
        }
        importDialog.show()
    }



    fun showAlert(context: Context, message: String) {
        AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Ok", null)
                .show()
    }

    fun showDialog(context: Context, title: String, message: String) {
        Log.e(javaClass.simpleName, message)
        AlertDialog.Builder(context, de.hartz.software.parannoying.core.R.style.AlertDialog)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok", null)
                .show()
    }

    fun showYesNoAlert(context: Context, message: String, dialogClickListener: DialogInterface.OnClickListener) {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        builder.setMessage(message).setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show()
    }

    // TODO: Beautify some how..
    open class InputDialogCallback : DialogInterface.OnClickListener {

        lateinit var input: EditText

        override fun onClick(dialog: DialogInterface?, which: Int) {
            if (which == DialogInterface.BUTTON_POSITIVE) {
                onFinish(input.text.toString())
            } else if (which == DialogInterface.BUTTON_NEGATIVE || which == DialogInterface.BUTTON_NEUTRAL) {
                onCancel()
            }
        }

        open fun onCancel () {

        }

        open fun onFinish(input: String) {

        }

    }

    fun showInputDialog(context: Context, text: String, freeText: Boolean, listener: InputDialogCallback) {
        val builder = AlertDialog.Builder(context, de.hartz.software.parannoying.core.R.style.AlertDialog)
        builder.setTitle(text)

        // Allow multiline title.
        val textView = TextView(context)
        textView.text = text
        val top = getTopPadding(context)
        val side = getSidePadding(context)
        textView.setPadding(side,top,side,top)
        textView.setTypeface(null, Typeface.BOLD)
        builder.setCustomTitle(textView)

        val params = ActionBar.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val view = LinearLayout(context)
        view.layoutParams = params
        view.setPadding(side, top, side, top)
        val input = EditText(context)
        input.tag = INPUT_DIALOG_ID
        input.layoutParams = params
        listener.input = input
        // TODO: Handle via Enum
        if (freeText) {
            input.inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                    InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
        } else {
            input.inputType = InputType.TYPE_CLASS_TEXT
        }
        view.addView(input)
        builder.setView(view)

        builder.setPositiveButton("OK", listener)
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.setOnCancelListener { listener.onCancel() } // call cancle when clicking outside of the dialog.

        val dialog = builder.create()
        dialog.show()
        doKeepDialog(dialog)
    }


    fun showPasswordDialog(context: Context, text: String, listener: InputDialogCallback) {
        val builder = AlertDialog.Builder(context, de.hartz.software.parannoying.core.R.style.AlertDialog)
        builder.setTitle(text)

        val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout: View = inflater.inflate(R.layout.alert_password, null)
        val password1 = layout.findViewById(R.id.EditText_Pwd1) as EditText
        listener.input = password1
        val password2 = layout.findViewById(R.id.EditText_Pwd2) as EditText
        val error: TextView = layout.findViewById(R.id.TextView_PwdProblem) as TextView
        val indicator : View = layout.findViewById(R.id.indicator) as View
        builder.setView(layout)

        builder.setPositiveButton("OK", listener)
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        val dialog = builder.create()
        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                val strPass1: String = password1.getText().toString()
                val strPass2: String = password2.getText().toString()

                val distinctElements = strPass1.toCharArray().toHashSet().size

                error.visibility = View.VISIBLE
                if (strPass1 != strPass2 && strPass2.isNotEmpty()) {
                    error.setText("Passwords differ")
                } else if (strPass1.length < 9 || strPass1.length * 0.33 > distinctElements) {
                    error.setText("Password is weak")
                } else {
                    error.visibility = View.INVISIBLE
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false)
                if (strPass1 == strPass2) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true)
                }
                val ratio = Math.min(1.0f, strPass1.length / 15.0f)
                indicator.setBackgroundColor(UiHelper.getTrafficLightColor(ratio, context))
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        }

        password1.addTextChangedListener(watcher)
        password2.addTextChangedListener(watcher)

        dialog.show()
        doKeepDialog(dialog)
    }

    // https://stackoverflow.com/a/27311231/8524651
    private fun doKeepDialog(dialog: Dialog) {
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes = lp
    }

    private fun getTopPadding(context: Context): Int {
        val dpi = context.getResources().getDisplayMetrics().density
        return (15 * dpi).toInt()
    }

    private fun getSidePadding(context: Context): Int {
        val dpi = context.getResources().getDisplayMetrics().density
        return (20 * dpi).toInt()
    }
}