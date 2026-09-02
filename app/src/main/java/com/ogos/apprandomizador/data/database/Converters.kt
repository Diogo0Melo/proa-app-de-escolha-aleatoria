package com.ogos.apprandomizador.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ogos.apprandomizador.model.RaffleItem
import java.time.Instant

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromRaffleItemList(value: List<RaffleItem>): String = gson.toJson(value)

    @TypeConverter
    fun toRaffleItemList(value: String): List<RaffleItem> {
        val type = object : TypeToken<List<RaffleItem>>() {}.type
        return gson.fromJson(value, type) ?: emptyList()
    }

    @TypeConverter
    fun fromHistory(value: List<String>?): String = gson.toJson(value)

    @TypeConverter
    fun toHistory(value: String?): List<String>? {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromInstantList(value: List<Instant>?): String = gson.toJson(value)

    @TypeConverter
    fun toInstantList(value: String?): List<Instant>? {
        val type = object : TypeToken<List<Instant>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromInstant(value: Instant?): Long? =
        value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }
}
