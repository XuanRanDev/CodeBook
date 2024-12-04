package dev.xuanran.codebook.util

import dev.xuanran.codebook.activity.MainActivity
import java.lang.RuntimeException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec


object PasswordUtils {
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16

    /**
     * 生成随机盐值。
     *
     * @return 随机盐值
     */
    @JvmStatic
    fun generateSalt(): ByteArray {
        val random = SecureRandom()
        val salt = ByteArray(PasswordUtils.SALT_LENGTH)
        random.nextBytes(salt)
        return salt
    }

    /**
     * 从用户提供的密码生成一个加密密钥。
     *
     * @param password 密码
     * @param salt 盐值
     * @return 密钥
     */
    @JvmStatic
    fun generateKeyFromPassword(password: String, salt: String): SecretKey {
        val salt2 = Base64.getDecoder().decode(salt.toByteArray())
        try {
            val passwordChars = password.toCharArray()
            val spec = PBEKeySpec(
                passwordChars,
                salt2,
                PasswordUtils.ITERATION_COUNT,
                PasswordUtils.KEY_LENGTH
            )
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = factory.generateSecret(spec).getEncoded()
            return SecretKeySpec(keyBytes, "AES")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to generate key from password", e)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException("Failed to generate key from password", e)
        }
    }


    /**
     * 从用户提供的密码生成一个加密密钥。
     *
     * @param password 密码
     * @return 密钥
     */
    @JvmStatic
    fun generateKeyFromPassword(password: String): SecretKey {
        try {
            val passwordChars = password.toCharArray()
            val spec = PBEKeySpec(
                passwordChars,
                MainActivity.salt,
                PasswordUtils.ITERATION_COUNT,
                PasswordUtils.KEY_LENGTH
            )
            val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = factory.generateSecret(spec).getEncoded()
            return SecretKeySpec(keyBytes, "AES")
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Failed to generate key from password", e)
        } catch (e: InvalidKeySpecException) {
            throw RuntimeException("Failed to generate key from password", e)
        }
    }
}