package com.ogos.apprandomizador.data.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromItemList(value: List<Map<String, Long>>): String =
        Json.encodeToString(value)

    @TypeConverter
    fun toItemList(value: String): List<Map<String, Long>> =
        Json.decodeFromString<List<Map<String, Long>>>(value).toList()

    @TypeConverter
    fun fromHistory(value: List<String>?): String = Json.encodeToString(value)

    @TypeConverter
    fun toHistory(value: String?): List<String>? = value?.let { Json.decodeFromString(it) }
}
