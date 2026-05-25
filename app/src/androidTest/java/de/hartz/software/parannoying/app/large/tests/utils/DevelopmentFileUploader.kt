package de.hartz.software.parannoying.app.large.tests.utils
import android.util.Log
import de.hartz.software.parannoying.core.interfaces.DevelopmentOnly
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

/*
Inspired by:
https://androidexample.com/upload-file-to-server

communicating with testdatareceiver-server.py
 */
@DevelopmentOnly
class DevelopmentFileUploader {

    var serverResponseCode: Int = 0
    var upLoadServerUri: String? = null

    init {
        // Port like set in the python file:
        // https://stackoverflow.com/a/1722427/8524651
        upLoadServerUri = "http://10.0.2.2:58128";
    }

    /*
    Testcode:
     */
    fun test() {
        val uploadFile = File.createTempFile("MyDummyTest", "Test")
        writeToFile("Dim dam TEs1132\n", uploadFile)
        uploadFile(uploadFile, "testFile.txt")
    }

    private fun writeToFile(data: String, file: File) {
        try {
            val outputStreamWriter =
                OutputStreamWriter(FileOutputStream(file))
            outputStreamWriter.write(data)
            outputStreamWriter.close()
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }

    fun uploadFile(sourceFile: File, targetFileName: String): Int {
        val sourceFileUri = sourceFile.absolutePath
        val fileName = targetFileName;

        var conn: HttpURLConnection? = null
        var dos: DataOutputStream? = null
        val lineEnd: String = "\r\n";
        val twoHyphens: String = "--";
        val boundary = "*****";

        var bytesRead:Int
        var bytesAvailable:Int
        var bufferSize:Int

        var buffer: ByteArray
        val maxBufferSize = 1 * 1024 * 1024;
        val sourceFile = File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Log.e("uploadFile", "Source File not exist :"
                    + sourceFile.absolutePath);
            return 0;

        }
        // open a URL connection to the Servlet
        val fileInputStream = FileInputStream(sourceFile);
        val url = URL(upLoadServerUri);

        // Open a HTTP  connection to  the URL
        conn = url.openConnection() as HttpURLConnection
        conn.setDoInput(true); // Allow Inputs
        conn.setDoOutput(true); // Allow Outputs
        conn.setUseCaches(false); // Don't use a Cached Copy
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Connection", "Keep-Alive");
        conn.setRequestProperty("ENCTYPE", "multipart/form-data");
        conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        conn.setRequestProperty("file", fileName);

        dos = DataOutputStream(conn.getOutputStream());

        dos.writeBytes(twoHyphens + boundary + lineEnd);
        dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + fileName + "\"" + lineEnd);

        dos.writeBytes(lineEnd);

        // create a buffer of  maximum size
        bytesAvailable = fileInputStream.available();

        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        buffer = ByteArray(bufferSize);

        // read file and write it into form...
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        while (bytesRead > 0) {

         dos.write(buffer, 0, bufferSize);
         bytesAvailable = fileInputStream.available();
         bufferSize = Math.min(bytesAvailable, maxBufferSize);
         bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        }

        // send multipart form data necesssary after file data...
        dos.writeBytes(lineEnd);
        dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        // Responses from the server (code and message)
        serverResponseCode = conn.getResponseCode();
        val serverResponseMessage = conn.getResponseMessage();

        Log.i("uploadFile", "HTTP Response is : "
               + serverResponseMessage + ": " + serverResponseCode);

        if(serverResponseCode == 200){
            val msg = "File Upload Completed."
           Log.e("Upload file to server", msg);
        }

        //close the streams //
        fileInputStream.close();
        dos.flush();
        dos.close();
        return serverResponseCode;
     }
}