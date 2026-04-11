package com.ogos.apprandomizador.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Entity(tableName = "item_list")
data class ItemList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var topic: String = "",
    var items: MutableList<Map<String, Color>> = mutableListOf(),
    var uses: Int = 0,
    var resultHistory: MutableList<String> = mutableListOf(),
    var dateTimeHistory: MutableList<String> = mutableListOf()
) {
    var lastUse = "Nunca"
    val createdAt: String
        get() = setCreatedDateTime()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            startUpdateLoop()
        }
    }

    private suspend fun startUpdateLoop() {
        updateLastUse()
        while (true) {
            delay(60000)
            updateLastUse()
        }
    }

    private fun setCreatedDateTime(): String {
        val now = LocalDateTime.now()
        val format = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formatedDateTime = now.format(format)
        return formatedDateTime
    }

    fun updateLastUse() {
        if (dateTimeHistory.isEmpty())
            return

        val now = LocalDateTime.now()
        val lastSavedDateTimeHistory = dateTimeHistory.last()
        val dateTimeHistory = LocalDateTime.parse(lastSavedDateTimeHistory)

        val seconds = ChronoUnit.SECONDS.between(dateTimeHistory, now)
        val minutes = ChronoUnit.MINUTES.between(dateTimeHistory, now)
        val hours = ChronoUnit.HOURS.between(dateTimeHistory, now)
        val days = ChronoUnit.DAYS.between(dateTimeHistory, now)
        val month = ChronoUnit.MONTHS.between(dateTimeHistory, now)
        val year = ChronoUnit.YEARS.between(dateTimeHistory, now)

        val formatedDateTime = when {
            seconds < 60 -> "Agora mesmo"
            minutes < 60 -> "Há $minutes min"
            hours < 24 -> "Há $hours horas"
            days < 30 -> "Há $days dias"
            month < 12 -> "Há $month meses"
            else -> "Há $year anos"
        }
        lastUse = formatedDateTime
    }

    fun updateHistory(resultItem: String, resultDateTime: LocalDateTime) {
        resultHistory.add(resultItem)
        dateTimeHistory.add(resultDateTime.toString())
        updateLastUse()
    }
}
