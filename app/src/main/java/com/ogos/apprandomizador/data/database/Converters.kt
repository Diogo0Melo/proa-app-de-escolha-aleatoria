package com.ogos.apprandomizador.data.database

import androidx.room.TypeConverter
import com.ogos.apprandomizador.model.RaffleItem
import kotlinx.serialization.json.Json
import java.time.Instant

class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromRaffleItemList(value: List<RaffleItem>): String = json.encodeToString(value)

    @TypeConverter
    fun toRaffleItemList(value: String): List<RaffleItem> =
        json.decodeFromString(value)

    @TypeConverter
    fun fromInstant(value: Instant?): Long? =
        value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? =
        value?.let { Instant.ofEpochMilli(it) }
}
