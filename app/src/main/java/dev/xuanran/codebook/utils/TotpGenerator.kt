package dev.xuanran.codebook.utils

import com.eatthepath.otp.TimeBasedOneTimePasswordGenerator
import org.apache.commons.codec.binary.Base32
import java.time.Duration
import java.time.Instant
import javax.crypto.spec.SecretKeySpec

object TotpGenerator {
    private val base32 = Base32()
    
    private fun createTotpGenerator(
        algorithm: String = "SHA1",
        digits: Int = 6,
        period: Int = 30
    ): TimeBasedOneTimePasswordGenerator {
        val hashAlgorithm = when (algorithm.uppercase()) {
            "SHA1" -> "HmacSHA1"
            "SHA224" -> "HmacSHA224"
            "SHA256" -> "HmacSHA256"
            "SHA384" -> "HmacSHA384"
            "SHA512" -> "HmacSHA512"
            else -> throw IllegalArgumentException("Unsupported algorithm: $algorithm")
        }
        
        return TimeBasedOneTimePasswordGenerator(
            Duration.ofSeconds(period.toLong()),
            digits,
            hashAlgorithm
        )
    }

    fun generateTOTP(
        secret: String,
        algorithm: String = "SHA1",
        digits: Int = 6,
        period: Int = 30,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val decodedKey = base32.decode(secret.uppercase().replace(" ", ""))
        val secretKey = SecretKeySpec(decodedKey, "RAW")
        val totp = createTotpGenerator(algorithm, digits, period)
        val code = totp.generateOneTimePassword(secretKey, Instant.ofEpochMilli(timestamp))
        return String.format("%0${digits}d", code)
    }

    fun verifyTOTP(
        secret: String,
        code: String,
        algorithm: String = "SHA1",
        digits: Int = 6,
        period: Int = 30,
        timestamp: Long = System.currentTimeMillis()
    ): Boolean {
        return try {
            generateTOTP(secret, algorithm, digits, period, timestamp) == code
        } catch (e: Exception) {
            false
        }
    }
}