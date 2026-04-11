package com.ogos.apprandomizador.database

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class Converters {
    @TypeConverter
    fun fromItemList(value: MutableList<Map<String, Color>>): String {
        val list = value.map { map ->
            map.mapValues { it.value.toArgb() }
        }
        return Json.encodeToString(list)
    }

    @TypeConverter
    fun toItemList(value: String): MutableList<Map<String, Color>> {
        val list = Json.decodeFromString<List<Map<String, Int>>>(value)
        return list.map { map ->
            map.mapValues { Color(it.value) }
        }.toMutableList()
    }

    @TypeConverter
    fun fromHistory(value: MutableList<String>?): String = Json.encodeToString(value)

    @TypeConverter
    fun toHistory(value: String?): MutableList<String>? = value?.let { Json.decodeFromString(it) }
}
