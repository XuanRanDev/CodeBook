package dev.xuanran.codebook.service.impl;

import static dev.xuanran.codebook.bean.Constants.IV_SIZE;
import static dev.xuanran.codebook.bean.Constants.TAG_SIZE;
import static dev.xuanran.codebook.bean.Constants.TRANSFORMATION;

import android.util.Base64;

import androidx.annotation.NonNull;

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
import dev.xuanran.codebook.util.AESUtils;
import dev.xuanran.codebook.util.PasswordUtils;

/**
 * 密码加密策略
 */
public class PasswordCipherStrategy implements CipherStrategy {

    private final SecretKey secretKey;

    /**
     * 自定义密码加密构造函数
     * 该函数会通过用户给到的密码生成高强度的加密
     * @param password 用户密码
     */
    public PasswordCipherStrategy(String password) {
        this.secretKey = PasswordUtils.generateKeyFromPassword(password);
    }

    /**
     * 加密数据
     *
     * @param data 要加密的数
     * @return 加密后的数据
     * @throws Exception 加密错误
     */
    @Override
    public String encryptData(String data) {
        return AESUtils.encrypt(secretKey, data);
    }

    /**
     * 解密数据
     *
     * @param encryptedData 要解密的数据
     * @return 解密后的数据
     * @throws Exception 解密错误
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
    private String decryptingData(String encryptedData) throws Exception {
        return AESUtils.decrypt(secretKey, encryptedData);
    }

}
