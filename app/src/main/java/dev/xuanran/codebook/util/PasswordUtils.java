package dev.xuanran.codebook.util;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class PasswordUtils {

    /**
     * 迭代次数决定了 PBKDF2 算法执行的次数。每次迭代都会对输入密码进行哈希运算，这个过程会重复 ITERATION_COUNT 次。
     * 通过增加迭代次数，可以增加密钥导出过程的计算量，从而增加暴力破解的难度。换句话说，较高的迭代次数可以提高安全性，但也会增加生成密钥所需的时间。
     */
    private static final int ITERATION_COUNT = 10000;

    /**
     * 密钥长度决定了生成密钥的位数。对于 AES 加密算法，常见的密钥长度是 128 位、192 位和 256 位。
     * 密钥越长，理论上加密强度越高，但也会增加计算开销。在大多数情况下，128 位或 256 位密钥长度是常用的选择。
     */
    private static final int KEY_LENGTH = 256;

    /**
     * 从用户提供的密码生成一个加密密钥。
     * 这个方法使用了 PBKDF2 (Password-Based Key Derivation Function 2) 算法，它能从密码中派生出强密钥。
     *
     * @param password 密码
     * @return 给定相同的密码和盐，PBKDF2 算法每次生成的密钥是相同的。
     * 这意味着，如果你使用相同的密码和相同的盐，generateKeyFromPassword 方法将生成相同的密钥。
     */
    public static SecretKey generateKeyFromPassword(String password) {
        try {
            char[] passwordChars = password.toCharArray();
            byte[] salt = new byte[16];
            PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, ITERATION_COUNT, KEY_LENGTH);
            // 使用指定的算法（PBKDF2WithHmacSHA256）生成一个密钥工厂，并生成密钥字节数组。
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            // 使用生成的密钥字节数组和算法（AES）创建一个 SecretKeySpec 实例
            return new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Failed to generate key from password", e);
        }
    }
}
