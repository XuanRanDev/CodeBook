package dev.xuanran.codebook.util;

import javax.crypto.KeyGenerator;

public class FingerprintUtils {
    private static final String KEY_NAME = "your_key_name";
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;

    private void initializeKeystoreAndKeyGenerator() throws Exception {
        keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);

        keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(
                new KeyGenParameterSpec.Builder(KEY_NAME,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .setUserAuthenticationRequired(true)
                        .build());
        keyGenerator.generateKey();
    }

}
