package dev.xuanran.codebook.util

import android.util.Base64
import java.lang.Exception
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private const val ALGORITHM = "AES"
    private const val KEY_SIZE = 128

    @Throws(Exception::class)
    fun encrypt(data: String, password: String): String? {
        val key = CryptoUtils.generateKey(password)
        val cipher = Cipher.getInstance(CryptoUtils.ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedData = cipher.doFinal(data.toByteArray(charset("UTF-8")))
        return Base64.encodeToString(encryptedData, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun decrypt(data: String?, password: String): String {
        val key = CryptoUtils.generateKey(password)
        val cipher = Cipher.getInstance(CryptoUtils.ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedData = Base64.decode(data, Base64.DEFAULT)
        val decryptedData = cipher.doFinal(decodedData)
        return kotlin.text.String(decryptedData!!, charset("UTF-8"))
    }

    @Throws(Exception::class)
    private fun generateKey(password: String): SecretKeySpec {
        val keyGen = KeyGenerator.getInstance(CryptoUtils.ALGORITHM)
        val secureRandom = SecureRandom(password.toByteArray())
        keyGen.init(CryptoUtils.KEY_SIZE, secureRandom)
        val secretKey = keyGen.generateKey()
        return SecretKeySpec(secretKey.getEncoded(), CryptoUtils.ALGORITHM)
    }
}
