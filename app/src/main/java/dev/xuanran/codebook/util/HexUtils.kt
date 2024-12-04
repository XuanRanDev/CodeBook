package dev.xuanran.codebook.util

import java.lang.StringBuilder

object HexUtils {
    /**
     * 将字节数组转换为十六进制字符串。
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    @JvmStatic
    fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuilder()
        for (b in bytes) {
            sb.append(String.format("%02x", b))
        }
        return sb.toString()
    }

    /**
     * 将十六进制字符串转换为字节数组。
     *
     * @param hex 十六进制字符串
     * @return 字节数组
     */
    @JvmStatic
    fun hexToBytes(hex: String): ByteArray {
        val length = hex.length
        val bytes = ByteArray(length / 2)
        var i = 0
        while (i < length) {
            bytes[i / 2] = ((hex.get(i).digitToIntOrNull(16) ?: -1 shl 4)
            + hex.get(i + 1).digitToIntOrNull(16) ?: -1).toByte()
            i += 2
        }
        return bytes
    }
}
