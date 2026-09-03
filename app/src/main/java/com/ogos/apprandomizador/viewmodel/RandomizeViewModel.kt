package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.data.repository.ItemListRepository
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.model.RaffleItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds

class RandomizeViewModel(private val repository: ItemListRepository) : ViewModel() {

    private val secureRandom = SecureRandom()
    private val _currentItemList = MutableStateFlow(ItemList())
    val currentItemList: StateFlow<ItemList> = _currentItemList.asStateFlow()
    private val _currentRandomItem = MutableStateFlow<RaffleItem>(RaffleItem("", 0))
    val currentRandomItem: StateFlow<RaffleItem> = _currentRandomItem.asStateFlow()
    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    fun startSpinning() {
        _isSpinning.value = true
    }

    fun stopSpinning() {
        _isSpinning.value = false
    }

    fun performRoll() {
        val item = currentItemList.value

        if (item.items.isEmpty()) return
        viewModelScope.launch {
            rollAnimation()

            val newIndex = secureRandom.nextInt(item.items.size)
            val updatedItem = item.recordDraw(item.items[newIndex])

            _currentRandomItem.value = item.items[newIndex]
            _currentItemList.value = updatedItem
            stopSpinning()
            updateItem(updatedItem)
        }
    }

    suspend fun rollAnimation() {
        val itemList = currentItemList.value
        val range = itemList.items.size
        if (range <= 0) return
        startSpinning()
        repeat(69) { index ->
            val sleepTime = when {
                index < 64 -> 15L
                else -> (index - 64 + 1) * 40L
            }
            val newIndex = Random.nextInt(range)
            _currentRandomItem.value = itemList.items[newIndex]
            delay(sleepTime.milliseconds)
        }
    }

    fun defaultText(color: Long) {
        _currentRandomItem.value = RaffleItem("USE RODAR PARA SORTEAR", color)
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