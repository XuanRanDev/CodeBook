package dev.xuanran.codebook.util;

import static dev.xuanran.codebook.bean.Constants.IV_SIZE;
import static dev.xuanran.codebook.bean.Constants.TAG_SIZE;
import static dev.xuanran.codebook.bean.Constants.TRANSFORMATION;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AESUtils {
    public static String encrypt(SecretKey secretKey, String data) {
        try {
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

    public static String decrypt(SecretKey secretKey, String data) throws Exception {
        byte[] decoded = Base64.decode(data, Base64.DEFAULT);

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
}


