package dev.xuanran.codebook.util

import android.util.Base64
import dev.xuanran.codebook.bean.Constants
import java.lang.Exception
import java.lang.RuntimeException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object AESUtils {
    @JvmStatic
    fun encrypt(secretKey: SecretKey?, data: String): String? {
        try {
            val cipher = Cipher.getInstance(Constants.TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.getIV()
            val encryption = cipher.doFinal(data.toByteArray())
            val combined = ByteArray(Constants.IV_SIZE + encryption.size)

            System.arraycopy(iv, 0, combined, 0, Constants.IV_SIZE)
            System.arraycopy(encryption, 0, combined, Constants.IV_SIZE, encryption.size)
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    @JvmStatic
    @Throws(Exception::class)
    fun decrypt(secretKey: SecretKey?, data: String?): String {
        val decoded = Base64.decode(data, Base64.DEFAULT)

        val iv = ByteArray(Constants.IV_SIZE)
        System.arraycopy(decoded, 0, iv, 0, Constants.IV_SIZE)

        val encryption = ByteArray(decoded.size - Constants.IV_SIZE)
        System.arraycopy(decoded, Constants.IV_SIZE, encryption, 0, encryption.size)

        val cipher = Cipher.getInstance(Constants.TRANSFORMATION)
        val spec = GCMParameterSpec(Constants.TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val decrypted = cipher.doFinal(encryption)
        return String(decrypted)
    }
}


