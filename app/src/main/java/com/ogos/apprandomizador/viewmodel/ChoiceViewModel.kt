package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.repository.ItemListRepository
import kotlinx.coroutines.flow.Flow
import java.security.SecureRandom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class ChoiceViewModel(private val repository: ItemListRepository) : ViewModel() {

    private val secureRandom = SecureRandom()
    private val _currentItemList = MutableStateFlow(ItemList())
    val currentItemList: StateFlow<ItemList> = _currentItemList.asStateFlow()
    private val _currentRandomItem = MutableStateFlow<Map<String, Long>>(mapOf())
    val currentRandomItem: StateFlow<Map<String, Long>> = _currentRandomItem

    fun performRoll() {
        val item = currentItemList.value

        if (item.items.isEmpty()) return

        val newIndex = secureRandom.nextInt(item.items.size)
        val resultKey = item.items[newIndex].keys.first()
        val newHistory = item.resultHistory.toMutableList().apply { add(resultKey) }
        val newTimeHistory = item.dateTimeHistory.toMutableList().apply { add(LocalDateTime.now().toString()) }
        val updatedItem = item.copy(
            uses = item.uses + 1,
            resultHistory = newHistory,
            dateTimeHistory = newTimeHistory
        )

        _currentRandomItem.value = item.items[newIndex]
        _currentItemList.value = updatedItem
        updateItem(updatedItem)
    }

    fun defaultText(color: Long) {
        _currentRandomItem.value = mapOf("USE RODAR PARA SORTEAR" to color)
    }

    fun setCurrentItem(id: Long) {
        viewModelScope.launch {
            _currentItemList.value = repository.getItem(id)
        }
    }

    fun updateItem(item: ItemList) {
        viewModelScope.launch {
            repository.updateInDatabase(item)
        }
    }

    fun insertItem(item: ItemList) {
        viewModelScope.launch {
            repository.saveInDatabase(item)
        }
    }

    fun getAllItems(): Flow<List<ItemList>> = repository.allItems
}