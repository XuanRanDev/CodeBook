package dev.xuanran.codebook.service.impl;

import static dev.xuanran.codebook.bean.Constants.CIPHER_KEYSTORE_ALIAS;
import static dev.xuanran.codebook.bean.Constants.FINGERPRINT_AUTH_EXPIRED;

import android.app.Activity;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.security.KeyStore;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import dev.xuanran.codebook.R;
import dev.xuanran.codebook.callback.FingerprintCallback;
import dev.xuanran.codebook.service.CipherStrategy;
import dev.xuanran.codebook.util.AESUtils;

public class FingerprintCipherStrategy implements CipherStrategy {

    private final Activity context;
    private boolean needGenKey;
    private final FingerprintCallback fingerprintCallback;
    private static SecretKey cachedSecretKey = null;
    private CancellationSignal cancellationSignal;
    private boolean isCallbackCalled;

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
        isCallbackCalled = false;
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
        return AESUtils.encrypt(getSecretKey(), data);
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
    private static String decryptingData(String encryptedData) throws Exception {
        return AESUtils.decrypt(getSecretKey(), encryptedData);
    }

    /**
     * 使用指纹验证进行身份验证
     */
    private void authenticateWithFingerprint() {
        // 创建 BottomSheetDialog 并加载自定义布局
        View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.fingerprint_bottom_sheet, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(bottomSheetView);

        // 设置自定义布局中的视图
        TextView titleTextView = bottomSheetView.findViewById(R.id.fingerprint_title);
        TextView subtitleTextView = bottomSheetView.findViewById(R.id.fingerprint_subtitle);
        ImageView fingerprintIcon = bottomSheetView.findViewById(R.id.fingerprint_icon);
        Button cancelButton = bottomSheetView.findViewById(R.id.cancel_button);

        // 取消按钮点击事件
        cancelButton.setOnClickListener(v -> {
            cancelFingerprintAuthentication();
            bottomSheetDialog.dismiss();
            if (isCallbackCalled) return;
            isCallbackCalled = true;
            fingerprintCallback.onFingerprint(false, -1, context.getString(R.string.user_cancel));
        });

        // 设置指纹管理器和回调
        FingerprintManager fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        cancellationSignal = new CancellationSignal();
        FingerprintManager.AuthenticationCallback authenticationCallback = new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                bottomSheetDialog.dismiss();
                if (isCallbackCalled) return;
                isCallbackCalled = true;
                fingerprintCallback.onFingerprint(false, -1, String.valueOf(errString));
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                bottomSheetDialog.dismiss();
                if (needGenKey) {
                    generateSecretKey();
                    needGenKey = false;
                }
                fingerprintCallback.onFingerprint(true, 0, "OK");
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                if (isCallbackCalled) return;
                isCallbackCalled = true;
                fingerprintCallback.onFingerprint(false, -2, context.getString(R.string.auth_fail));
            }
        };

        // 开始指纹认证
        bottomSheetDialog.setOnShowListener(dialog -> {
            fingerprintManager.authenticate(null, cancellationSignal, 0, authenticationCallback, null);
        });

        // 显示 BottomSheetDialog
        bottomSheetDialog.show();
    }

    public void cancelFingerprintAuthentication() {
        if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
            cancellationSignal.cancel();
        }
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