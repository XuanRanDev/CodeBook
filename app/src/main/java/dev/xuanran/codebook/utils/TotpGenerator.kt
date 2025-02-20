package dev.xuanran.codebook.utils

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.floor

object TotpGenerator {
    fun generateTOTP(
        secret: String,
        algorithm: String = "SHA1",
        digits: Int = 6,
        period: Int = 30
    ): String {
        val counter = floor(System.currentTimeMillis() / 1000.0 / period).toLong()
        return generateTOTP(secret, counter, algorithm, digits)
    }

    private fun generateTOTP(
        secret: String,
        counter: Long,
        algorithm: String,
        digits: Int
    ): String {
        val decodedKey = Base64.decode(secret, Base64.DEFAULT)
        val hash = when (algorithm.uppercase()) {
            "SHA1" -> "HmacSHA1"
            "SHA256" -> "HmacSHA256"
            "SHA512" -> "HmacSHA512"
            else -> throw IllegalArgumentException("Unsupported algorithm: $algorithm")
        }

        val mac = Mac.getInstance(hash)
        mac.init(SecretKeySpec(decodedKey, "RAW"))

        val time = ByteArray(8)
        var value = counter
        for (i in 7 downTo 0) {
            time[i] = (value and 0xffL).toByte()
            value = value shr 8
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

    fun getRemainingSeconds(period: Int): Int {
        return period - (System.currentTimeMillis() / 1000 % period).toInt()
    }
} 