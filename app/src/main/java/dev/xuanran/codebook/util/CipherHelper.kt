package dev.xuanran.codebook.util

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import dev.xuanran.codebook.bean.Constants
import java.lang.Exception
import java.lang.RuntimeException
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

object CipherHelper {
    private var cachedSecretKey: SecretKey? = null

    /**
     * 在设备的 Android 密钥库（AndroidKeyStore）中生成一个随机的 AES 密钥。
     * 这个密钥可以用于加密和解密数据，并且该密钥受设备的生物识别（如指纹）保护。
     * 这意味着每次要使用这个密钥时，都需要通过生物识别。
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    fun generateSecretKey() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    Constants.CIPHER_KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(false)
                    .setUserAuthenticationParameters(60, KeyProperties.AUTH_BIOMETRIC_STRONG)
                    .build()
            )

            CipherHelper.cachedSecretKey = keyGenerator.generateKey()
        } catch (e: Exception) {
            throw RuntimeException("无法生成密钥。")
        }
    }


    fun getSecretKey(): SecretKey? {
        if (CipherHelper.cachedSecretKey == null) {
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                CipherHelper.cachedSecretKey =
                    keyStore.getKey(Constants.CIPHER_KEYSTORE_ALIAS, null) as SecretKey?
            } catch (e: Exception) {
                throw RuntimeException("授权已过期或无法获取密钥", e)
            }
        }
        return CipherHelper.cachedSecretKey
    }
}
