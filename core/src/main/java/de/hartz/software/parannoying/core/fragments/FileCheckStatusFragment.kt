package de.hartz.software.parannoying.core.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import de.hartz.software.parannoying.core.R
import de.hartz.software.parannoying.core.extensions.app
import de.hartz.software.parannoying.core.interfaces.di.security.FileAnalyzerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class FileCheckStatusFragment : DialogFragment() {
    var callback: () -> Unit = {}
    lateinit var file: File

    private lateinit var actionButton: Button

    @Inject
    lateinit var fileAnalyzerHelper: FileAnalyzerHelper

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val root = LayoutInflater.from(context).inflate(R.layout.fragment_file_check, null)
        actionButton = root.findViewById(R.id.actionButton)
        root.findViewById<Button>(R.id.cancleButton).setOnClickListener {
            dismiss()
        }

        app.coreComponents.inject(this)

        actionButton.setOnClickListener {
            callback()
        }
        Log.e(javaClass.simpleName, "checking file123123")



        val view = AlertDialog.Builder(requireContext())
            .setView(root)
            .create()



        // Safe to use viewLifecycleOwner and requireView() here
        lifecycleScope.launch {
            Log.e(javaClass.simpleName, "checking file")
            val result = withContext(Dispatchers.IO) {
                Log.e(javaClass.simpleName, "checking file2")
                fileAnalyzerHelper.scan(file, requireContext())
            }
            Log.e(javaClass.simpleName, "checking file3")

            // Safely update the UI
            view.findViewById<View>(R.id.loading)?.visibility = View.GONE
            view.findViewById<TextView>(R.id.result)?.text = """
            Total: ${result.total}
            Suspicious text content: ${result.regexFileScannerScanner}
            Suspicious filetype content: ${result.formatScore}
            Suspicious data entropy: ${result.entropyScore}
            Suspicious file size: ${result.overflowScore}
            Suspicious commands: ${result.patternScore}
        """.trimIndent()
        }

        return view
    }


    // TODO Remove
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(javaClass.simpleName, "checking file112")

        Handler(Looper.getMainLooper()).post {
            Log.e(javaClass.simpleName, "checking file")
            val result = fileAnalyzerHelper.scan(file, requireContext())
            Log.e(javaClass.simpleName, "checking file3")

            // Safely update the UI
            view.findViewById<View>(R.id.loading).visibility = View.GONE
            view.findViewById<TextView>(R.id.result).text = """
            Total: ${result.total}
            Suspicious text content: ${result.regexFileScannerScanner}
            Suspicious filetype content: ${result.formatScore}
            Suspicious data entropy: ${result.entropyScore}
            Suspicious file size: ${result.overflowScore}
            Suspicious commands: ${result.patternScore}
        """.trimIndent()
        }

        // Safe to use viewLifecycleOwner and requireView() here
        viewLifecycleOwner.lifecycleScope.launch {
            Log.e(javaClass.simpleName, "checking file")
            val result = withContext(Dispatchers.IO) {
                Log.e(javaClass.simpleName, "checking file2")
                fileAnalyzerHelper.scan(file, requireContext())
            }
            Log.e(javaClass.simpleName, "checking file3")

            // Safely update the UI
            view.findViewById<View>(R.id.loading).visibility = View.GONE
            view.findViewById<TextView>(R.id.result).text = """
            Total: ${result.total}
            Suspicious text content: ${result.regexFileScannerScanner}
            Suspicious filetype content: ${result.formatScore}
            Suspicious data entropy: ${result.entropyScore}
            Suspicious file size: ${result.overflowScore}
            Suspicious commands: ${result.patternScore}
        """.trimIndent()
        }
    }

    /*
fun analyzeFile(file: File) {
    viewLifecycleOwner.lifecycleScope.launch {
        withContext(Dispatchers.IO) {
            val result = fileAnalyzerHelper.scan(file, requireContext())
            withContext(Dispatchers.Main) {
                val rootView = requireView() // Safe now — we're in the view lifecycle
                rootView.findViewById<View>(R.id.loading).visibility = View.GONE
                rootView.findViewById<TextView>(R.id.result).text = """
            Total: ${result.total}
            Suspicious text content: ${result.regexFileScannerScanner}
            Suspicious filetype content: ${result.formatScore}
            Suspicious data entropy: ${result.entropyScore}
            Suspicious file size: ${result.overflowScore}
            Suspicious commands: ${result.patternScore}
        """.trimIndent()
            }
        }
    }

    Handler(Looper.getMainLooper()).post {
        val result = fileAnalyzerHelper.scan(file, requireContext())
        val rootView = requireView()
        rootView.findViewById<View>(R.id.loading).visibility = View.GONE
        rootView.findViewById<TextView>(R.id.result).text = """
            Total: ${result.total}
            Suspicous textcontent: ${result.regexFileScannerScanner}
            Suspicous filetypecontent: ${result.formatScore}
            Suspicous data entropy: ${result.entropyScore}
            Suspicous file size: ${result.overflowScore}
            Suspicous commands: ${result.patternScore}
            """.trimIndent()

    }

    }
     */
}