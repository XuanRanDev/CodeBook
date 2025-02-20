package dev.xuanran.codebook.utils

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor

object TotpGenerator {
    private const val DEFAULT_TIME_STEP = 30
    private const val DEFAULT_DIGITS = 6
    private const val DEFAULT_ALGORITHM = "SHA1"
    private const val ALLOWABLE_TIME_DRIFT_SECONDS = 1

    fun generateTOTP(
        secret: String,
        algorithm: String = DEFAULT_ALGORITHM,
        digits: Int = DEFAULT_DIGITS,
        period: Int = DEFAULT_TIME_STEP,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        require(secret.isNotEmpty()) { "Secret cannot be empty" }
        require(digits in 6..8) { "Digits must be between 6 and 8" }
        require(period > 0) { "Period must be positive" }

        val counter = (timestamp / 1000 / period).toLong()
        return generateTOTP(secret, counter, algorithm, digits)
    }

    private fun generateTOTP(
        secret: String,
        counter: Long,
        algorithm: String,
        digits: Int
    ): String {
        val decodedKey = try {
            Base32().decode(secret.uppercase().replace(" ", ""))
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Base32 secret", e)
        }

        val hash = when (algorithm.uppercase()) {
            "SHA1" -> "HmacSHA1"
            "SHA256" -> "HmacSHA256"
            "SHA512" -> "HmacSHA512"
            else -> throw IllegalArgumentException("Unsupported algorithm: $algorithm")
        }

        val mac = try {
            Mac.getInstance(hash).apply {
                init(SecretKeySpec(decodedKey, "RAW"))
            }
        } catch (e: Exception) {
            throw IllegalStateException("Failed to initialize HMAC", e)
        }

        val time = ByteArray(8).apply {
            var value = counter
            for (i in 7 downTo 0) {
                this[i] = (value and 0xffL).toByte()
                value = value shr 8
            }
        }

        val hmacResult = mac.doFinal(time)
        val offset = hmacResult[hmacResult.size - 1].toInt() and 0xf

        var binary = (hmacResult[offset].toInt() and 0x7f shl 24) or
                (hmacResult[offset + 1].toInt() and 0xff shl 16) or
                (hmacResult[offset + 2].toInt() and 0xff shl 8) or
                (hmacResult[offset + 3].toInt() and 0xff)

        var otp = binary % Math.pow(10.0, digits.toDouble()).toInt()
        return String.format("%0${digits}d", otp)
    }

    fun verifyTOTP(
        secret: String,
        code: String,
        algorithm: String = DEFAULT_ALGORITHM,
        digits: Int = DEFAULT_DIGITS,
        period: Int = DEFAULT_TIME_STEP,
        timestamp: Long = System.currentTimeMillis(),
        windowSize: Int = 1
    ): Boolean {
        require(code.length == digits) { "Code length must match digits parameter" }
        require(windowSize >= 0) { "Window size must be non-negative" }

        val currentCounter = timestamp / 1000 / period

        return (-windowSize..windowSize).any { window ->
            val calculatedCode = generateTOTP(
                secret,
                algorithm,
                digits,
                period,
                (currentCounter + window) * period * 1000
            )
            calculatedCode == code
        }
    }

    fun getRemainingSeconds(
        period: Int = DEFAULT_TIME_STEP,
        timestamp: Long = System.currentTimeMillis()
    ): Int {
        require(period > 0) { "Period must be positive" }
        return period - (timestamp / 1000 % period).toInt()
    }

    fun isTimeInSync(timestamp: Long = System.currentTimeMillis()): Boolean {
        val systemTime = System.currentTimeMillis()
        return Math.abs(timestamp - systemTime) / 1000 <= ALLOWABLE_TIME_DRIFT_SECONDS
    }

    // Base32 implementation
    private class Base32 {
        private val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
        private val PADDING = '='

        fun decode(input: String): ByteArray {
            var tmp = input.uppercase().trimEnd(PADDING)
            if (tmp.any { it !in ALPHABET }) {
                throw IllegalArgumentException("Invalid Base32 character")
            }

            val bits = tmp.map { ALPHABET.indexOf(it) }
                .fold(0L to mutableListOf<Byte>()) { (buffer, bytes), value ->
                    val newBuffer = (buffer shl 5) or value.toLong()
                    val newBytes = bytes.apply {
                        if (size < tmp.length * 5 / 8) {
                            add(((newBuffer shr (size * 8)) and 0xFF).toByte())
                        }
                    }
                    newBuffer to newBytes
                }.second

            return bits.toByteArray()
        }
    }
}