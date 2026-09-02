package com.ogos.apprandomizador.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Entity(tableName = "item_list")
data class ItemList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var topic: String = "",
    var items: List<RaffleItem> = listOf(),
    var uses: Int = 0,
    var resultHistory: List<String> = listOf(),
    var dateTimeHistory: List<Instant> = listOf(),
    val createdAtNotFormatted: String = LocalDateTime.now().toString(),
) {
    val lastUse: String
        get() = updateLastUse()
    val createdAt: String
        get() = setCreatedDateTime(createdAtNotFormatted)

    private fun setCreatedDateTime(createdDateTime: String): String {
        val dateTime = LocalDateTime.parse(createdDateTime)
        val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return dateTime.format(format)
    }

    fun updateLastUse(): String {
        if (dateTimeHistory.isEmpty()) {
            return "Nunca"
        }

        val zoneId = ZoneId.systemDefault()

        val now = Instant.now().atZone(zoneId)
        val lastUse = dateTimeHistory.last().atZone(zoneId)

        val seconds = ChronoUnit.SECONDS.between(lastUse, now)
        val minutes = ChronoUnit.MINUTES.between(lastUse, now)
        val hours = ChronoUnit.HOURS.between(lastUse, now)
        val days = ChronoUnit.DAYS.between(lastUse, now)
        val months = ChronoUnit.MONTHS.between(lastUse, now)
        val years = ChronoUnit.YEARS.between(lastUse, now)

        return when {
            seconds < 60 -> "agora mesmo"
            minutes < 60 -> "há $minutes min"
            hours < 24 -> "há $hours horas"
            days < 30 -> "há $days dias"
            months < 12 -> "há $months meses"
            else -> "há $years anos"
        }
    }
}
