package dev.xuanran.codebook.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {
    @JvmStatic
    fun getNowTime(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return dateFormat.format(Date())
    }
}
