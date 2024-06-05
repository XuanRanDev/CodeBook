package dev.xuanran.codebook.util;

import static dev.xuanran.codebook.activity.MainActivity.salt;
import static dev.xuanran.codebook.bean.Constants.KEY_SALT;

import android.content.Context;
import android.content.SharedPreferences;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;


public class PasswordUtils {

    private static final int ITERATION_COUNT = 10000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    /**
     * 生成随机盐值。
     *
     * @return 随机盐值
     */
    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }


    /**
     * 从用户提供的密码生成一个加密密钥。
     *
     * @param password 密码
     * @return 密钥
     */
    public static SecretKey generateKeyFromPassword(String password) {
        try {
            char[] passwordChars = password.toCharArray();
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to generate key from password", e);
        }
    }


}