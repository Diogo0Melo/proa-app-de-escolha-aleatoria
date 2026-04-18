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

    private val _randomNumber = MutableStateFlow(-1)
    val randomNumber: StateFlow<Int> = _randomNumber.asStateFlow()
    private val _currentItemList = MutableStateFlow(ItemList())
    val currentItemList: StateFlow<ItemList> = _currentItemList.asStateFlow()
    private val secureRandom = SecureRandom()
    private val _currentRandomItem = MutableStateFlow<Map<String, Long>?>(null)
    val currentRandomItem: StateFlow<Map<String, Long>?> = _currentRandomItem

    fun rollList(range: Int, item: ItemList, color: Long, isElseItem: Boolean = false) {
        if (!isElseItem) _randomNumber.value = secureRandom.nextInt(range)
        _currentRandomItem.value = when {
            randomNumber.value in 0..range -> {
                val result = item.items[randomNumber.value]
                item.uses++
                item.updateHistory(result.keys.first(), LocalDateTime.now())
                result
            }

            else -> mapOf("USE RODAR PARA SORTEAR" to color)
        }
        updateItem(item)
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