package com.ogos.apprandomizador.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromItemList(value: MutableList<Map<String, Long>>): String =
        Json.encodeToString(value)

    @TypeConverter
    fun toItemList(value: String): MutableList<Map<String, Long>> =
        Json.decodeFromString<List<Map<String, Long>>>(value).toMutableList()

    @TypeConverter
    fun fromHistory(value: MutableList<String>?): String = Json.encodeToString(value)

    @TypeConverter
    fun toHistory(value: String?): MutableList<String>? = value?.let { Json.decodeFromString(it) }
}
