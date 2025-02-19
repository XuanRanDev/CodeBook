package dev.xuanran.codebook.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64

class Encryption(private val context: Context) {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val masterKeyAlias = "_codebook_master_key_"
    private val encryptedPrefs by lazy { createEncryptedSharedPreferences() }

    init {
        if (!keyStore.containsAlias(masterKeyAlias)) {
            createMasterKey()
        }
    }

    private fun createMasterKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            masterKeyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    private fun createEncryptedSharedPreferences() = EncryptedSharedPreferences.create(
        context,
        "codebook_secure_prefs",
        getMasterKey(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun getMasterKey(): MasterKey {
        return MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = keyStore.getKey(masterKeyAlias, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val combined = cipher.iv + encrypted // 组合IV和加密数据

        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedText: String): String {
        val combined = Base64.decode(encryptedText, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        
        // 从组合数据中提取IV和加密数据
        val iv = combined.slice(0..11).toByteArray()
        val encrypted = combined.slice(12..combined.lastIndex).toByteArray()

        val secretKey = keyStore.getKey(masterKeyAlias, null) as SecretKey
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

        return String(cipher.doFinal(encrypted), Charsets.UTF_8)
    }

    fun secureStore(key: String, value: String) {
        encryptedPrefs.edit().putString(key, value).apply()
    }

    fun secureRetrieve(key: String): String? {
        return encryptedPrefs.getString(key, null)
    }

    companion object {
        @Volatile
        private var INSTANCE: Encryption? = null

        fun getInstance(context: Context): Encryption {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Encryption(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
} 