package dev.xuanran.codebook.security

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESCrypt {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"

    fun encrypt(password: String, data: String): String {
        val key = generateKey(password)
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(16)
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
        val encrypted = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        return Base64.encodeToString(encrypted, Base64.DEFAULT)
    }

    fun decrypt(password: String, encryptedData: String): String {
        val key = generateKey(password)
        val cipher = Cipher.getInstance(ALGORITHM)
        val iv = ByteArray(16)
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
        val decrypted = cipher.doFinal(Base64.decode(encryptedData, Base64.DEFAULT))
        
        return String(decrypted, Charsets.UTF_8)
    }

    private fun generateKey(password: String): SecretKey {
        val keyBytes = password.toByteArray(Charsets.UTF_8).copyOf(32)
        return SecretKeySpec(keyBytes, "AES")
    }
} 