package com.ogos.apprandomizador.ui.format

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object TimeAgoFormatter {
    fun updateLastUse(dateTimeHistory: Instant?): String {
        if (dateTimeHistory == null) {
            return "Nunca"
        }

        val zoneId = ZoneId.systemDefault()

        val now = Instant.now().atZone(zoneId)
        val lastUse = dateTimeHistory.atZone(zoneId)

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
    fun formatCreatedAt(createdDateTime: String): String {
        val dateTime = LocalDateTime.parse(createdDateTime)
        val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")

        return dateTime.format(format)
    }
}
