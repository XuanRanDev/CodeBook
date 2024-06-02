package dev.xuanran.codebook.service.impl;

import static dev.xuanran.codebook.bean.Constants.CIPHER_KEYSTORE_ALIAS;
import static dev.xuanran.codebook.bean.Constants.FINGERPRINT_AUTH_EXPIRED;
import static dev.xuanran.codebook.bean.Constants.IV_SIZE;
import static dev.xuanran.codebook.bean.Constants.TAG_SIZE;
import static dev.xuanran.codebook.bean.Constants.TRANSFORMATION;

import android.app.Activity;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CountDownLatch;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import dev.xuanran.codebook.R;
import dev.xuanran.codebook.callback.FingerprintCallback;
import dev.xuanran.codebook.service.CipherStrategy;
import dev.xuanran.codebook.util.CipherHelper;

/**
 * 指纹认证加解密实现类
 */
public class FingerprintCipherStrategy implements CipherStrategy {

    private final Activity context;
    private boolean needGenKey;
    private final FingerprintCallback fingerprintCallback;
    private static SecretKey cachedSecretKey = null;


    /**
     * 指纹认证处理器
     *
     * @param context             context
     * @param fingerprintCallback 认证回调
     */
    public FingerprintCipherStrategy(Activity context, boolean needGenKey, FingerprintCallback fingerprintCallback) {
        this.context = context;
        this.needGenKey = needGenKey;
        this.fingerprintCallback = fingerprintCallback;
        authenticateWithFingerprint();
    }

    /**
     * 加密数据
     *
     * @param data 待加密数据
     * @return 加密后的数据
     * @throws Exception 加密异常
     */
    @Override
    public String encryptData(String data) {
        try {
            SecretKey secretKey = CipherHelper.getSecretKey();
            if (secretKey == null) {
                throw new RuntimeException("SecretKey is null");
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] iv = cipher.getIV();
            byte[] encryption = cipher.doFinal(data.getBytes());

            byte[] combined = new byte[IV_SIZE + encryption.length];
            System.arraycopy(iv, 0, combined, 0, IV_SIZE);
            System.arraycopy(encryption, 0, combined, IV_SIZE, encryption.length);

            return Base64.encodeToString(combined, Base64.DEFAULT);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 解密数据
     *
     * @param encryptedData 待解密数据
     * @return 解密后的数据
     * @throws Exception 解密异常
     */
    @Override
    public String decryptData(String encryptedData) {
        try {
            return decryptingData(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void validate(String encryptedData) throws Exception {
        decryptingData(encryptedData);
    }

    @NonNull
    private static String decryptingData(String encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKey secretKey = CipherHelper.getSecretKey();
        if (secretKey == null) {
            throw new RuntimeException("SecretKey is null");
        }
        byte[] decoded = Base64.decode(encryptedData, Base64.DEFAULT);

        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(decoded, 0, iv, 0, IV_SIZE);

        byte[] encryption = new byte[decoded.length - IV_SIZE];
        System.arraycopy(decoded, IV_SIZE, encryption, 0, encryption.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        byte[] decrypted = cipher.doFinal(encryption);
        return new String(decrypted);
    }


    /**
     * 使用指纹验证进行身份验证
     */
    private void authenticateWithFingerprint() {
        BiometricPrompt biometricPrompt = new BiometricPrompt((FragmentActivity) context,
                ContextCompat.getMainExecutor(context),
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        fingerprintCallback.onFingerprint(false, -1, String.valueOf(errString));
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        if (needGenKey) {
                            generateSecretKey();
                            needGenKey = false;
                        }
                        fingerprintCallback.onFingerprint(true, 0, "OK");
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        fingerprintCallback.onFingerprint(false, -1, "认证失败");
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(context.getString(R.string.fingerprint_title))
                .setSubtitle(context.getString(R.string.fingerprint_subtitle))
                .setNegativeButtonText(context.getString(R.string.cancel))
                .build();

        biometricPrompt.authenticate(promptInfo);
    }


    public static void generateSecretKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                throw new RuntimeException("Android 11+ required");
            }

            keyGenerator.init(new KeyGenParameterSpec.Builder(
                    CIPHER_KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(false)
                    .setUserAuthenticationParameters(FINGERPRINT_AUTH_EXPIRED, KeyProperties.AUTH_BIOMETRIC_STRONG)
                    .build());

            cachedSecretKey = keyGenerator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static SecretKey getSecretKey() {
        if (cachedSecretKey == null) {
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                keyStore.load(null);
                cachedSecretKey = (SecretKey) keyStore.getKey(CIPHER_KEYSTORE_ALIAS, null);
            } catch (Exception e) {
                throw new RuntimeException("授权已过期或无法获取密钥", e);
            }
        }
        return cachedSecretKey;
    }
}
