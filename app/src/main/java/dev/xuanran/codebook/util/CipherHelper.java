package dev.xuanran.codebook.util;

import static dev.xuanran.codebook.bean.Constants.CIPHER_KEYSTORE_ALIAS;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.KeyStore;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class CipherHelper {

    /**
     * 在设备的 Android 密钥库（AndroidKeyStore）中生成一个随机的 AES 密钥。
     * 这个密钥可以用于加密和解密数据，并且该密钥受设备的生物识别（如指纹）保护。
     * 这意味着每次要使用这个密钥时，都需要通过生物识别。
     */
    public static void generateSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    CIPHER_KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true)
                    .build());

            keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("无法生成在安全硬件中生成加密数据");
        }
    }


    public static SecretKey getSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            return ((SecretKey) keyStore.getKey(CIPHER_KEYSTORE_ALIAS, null));
        } catch (Exception e) {
            throw new RuntimeException("无法获取安全硬件中的加密数据");
        }
    }

}