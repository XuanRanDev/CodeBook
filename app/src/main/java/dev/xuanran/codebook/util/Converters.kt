package dev.xuanran.codebook.util

import androidx.room.TypeConverter
import java.util.Date

object Converters {
    @JvmStatic
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @JvmStatic
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return if (date == null) null else date.getTime()
    }
}
