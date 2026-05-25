package de.hartz.software.parannoying.offline.helper.security

import android.content.Context
import android.os.Environment
import android.util.Log
import de.hartz.software.parannoying.core.interfaces.di.security.AsymmetricEncryptionHelper
import de.hartz.software.parannoying.offline.helper.security.impl.asymmetric.rsa.PlainRSAEncryptionHelper
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.security.KeyPair
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

@Deprecated("Replace completley, use seed generation..")
class ConstantKeys(val context: Context, val asymmetricEncryptionHelper: AsymmetricEncryptionHelper) {

    companion object {
        const val numberOfKeyPairs = 1000
    }

    /**
     * Key: String format of public key.
     * Value: The public and private key.
     */
    var keyMap: HashMap<String, KeyPair>
    val keySize = asymmetricEncryptionHelper.KEY_SIZE_BITS
    val algorithmName = PlainRSAEncryptionHelper.GENERAL_ALGORITHM // TODO: Get algorithm from asymmetric helper..

    init {
        val file = context.assets.open("keys/RSA_2048_1000_2021-02-27.ser")
        keyMap = getKeyMap(numberOfKeyPairs, file)
        Log.e(javaClass.simpleName, "Loaded Keys: " + keyMap.size)
    }

    // Should not be called. Only needed for generating files once.
    fun createKeyPairs(n: Int) {
        val TAG = "createKeyPairs"

        val keyMap = HashMap<String, KeyPair>()
        for (i in 0..n) {
            val kp = asymmetricEncryptionHelper.getKeyPair()
            keyMap[asymmetricEncryptionHelper.publicKeyToString(kp.public)] = kp
            Log.e(TAG, "Generated Key" + i)
        }

        val fileName = getFileName(n)

        val file = File( testExportFileFolder(), fileName)

        val fout = FileOutputStream(file)
        val oos = ObjectOutputStream(fout)
        oos.writeObject(keyMap)

        val absolutePath: String = file.getAbsolutePath()
        Log.e(TAG, absolutePath)
    }

    fun getKeyByPublicKeyString(publicKeyString: String): KeyPair {
        return keyMap[publicKeyString]!!
    }

    fun getKeyMap(n: Int, fin: InputStream = getKeyMapInputStreamForTest(n)): HashMap<String, KeyPair> {
        val oos = ObjectInputStream(fin)
        return oos.readObject() as HashMap<String, KeyPair>
    }

    private fun getFileName (n: Int): String {
        val testCurrentFile = true
        if (testCurrentFile) {
            return "RSA_2048_1000_2021-02-27.ser"
        }
        val createNewFile = false
        if (createNewFile) {
            val df: DateFormat = SimpleDateFormat("yyyy-MM-dd")
            val nowAsISO: String = df.format(Date())
            return algorithmName + "_" + keySize + "_" + n + "_" + nowAsISO + ".ser"
        }
        return ""
    }

    private fun getKeyMapInputStreamForTest(n: Int): FileInputStream {
        return FileInputStream(File(testExportFileFolder(), getFileName(n)))
    }

    private fun testExportFileFolder(): File {
        val target = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        // val target =  File("src/test/resources/") // TODO: Doesnt work as base64 differs between android and java and isntrumentedtest gets excecuted on device storage
        if (!target.mkdirs()) {
            // throw IllegalArgumentException("Could not create folder in: " + System.getProperty("user.dir"))
        }
        return target
    }
}