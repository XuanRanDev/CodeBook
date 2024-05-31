package dev.xuanran.codebook.service.impl;

import static dev.xuanran.codebook.bean.Constants.IV_SIZE;
import static dev.xuanran.codebook.bean.Constants.TAG_SIZE;
import static dev.xuanran.codebook.bean.Constants.TRANSFORMATION;

import android.util.Base64;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import dev.xuanran.codebook.service.CipherStrategy;
import dev.xuanran.codebook.util.CipherHelper;

/**
 * 指纹认证加解密实现类
 */
public class FingerprintCipherStrategy implements CipherStrategy {

    /**
     * 加密数据
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
     * @param encryptedData 待解密数据
     * @return 解密后的数据
     * @throws Exception 解密异常
     */
    @Override
    public String decryptData(String encryptedData) {
        try {
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
