package com.ogos.apprandomizador.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Entity(tableName = "item_list")
data class ItemList(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var topic: String = "",
    var items: List<Map<String, Long>> = listOf(),
    var uses: Int = 0,
    var resultHistory: List<String> = listOf(),
    var dateTimeHistory: List<String> = listOf(),
    val createdAtNotFormated: String = LocalDateTime.now().toString(),
) {
    val lastUse: String
        get() = updateLastUse()
    val createdAt: String
        get() = setCreatedDateTime(createdAtNotFormated)

    private fun setCreatedDateTime(createdDateTime: String): String {
        val createdDateTime = LocalDateTime.parse(createdDateTime)
        val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatedDateTime = createdDateTime.format(format)
        return formatedDateTime
    }

    fun updateLastUse(): String {
        if (dateTimeHistory.isEmpty())
            return "Nunca"

        val now = LocalDateTime.now()
        val lastSavedDateTimeHistory = dateTimeHistory.last()
        val dateTimeHistory = LocalDateTime.parse(lastSavedDateTimeHistory)

        val seconds = ChronoUnit.SECONDS.between(dateTimeHistory, now)
        val minutes = ChronoUnit.MINUTES.between(dateTimeHistory, now)
        val hours = ChronoUnit.HOURS.between(dateTimeHistory, now)
        val days = ChronoUnit.DAYS.between(dateTimeHistory, now)
        val month = ChronoUnit.MONTHS.between(dateTimeHistory, now)
        val year = ChronoUnit.YEARS.between(dateTimeHistory, now)

        return when {
            seconds < 60 -> "agora mesmo"
            minutes < 60 -> "há $minutes min"
            hours < 24 -> "há $hours horas"
            days < 30 -> "há $days dias"
            month < 12 -> "há $month meses"
            else -> "há $year anos"
        }
    }
}
