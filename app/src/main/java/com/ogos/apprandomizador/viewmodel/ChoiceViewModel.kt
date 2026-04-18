package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ogos.apprandomizador.model.ItemList
import com.ogos.apprandomizador.repository.ItemListRepository
import java.security.SecureRandom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChoiceViewModel(private val repository: ItemListRepository) : ViewModel() {

    private val _randomNumber = MutableStateFlow(-1)
    val randomNumber: StateFlow<Int> = _randomNumber.asStateFlow()
    private val _currentItem = MutableStateFlow(ItemList())
    val currentItem: StateFlow<ItemList> = _currentItem.asStateFlow()
    private val secureRandom = SecureRandom()

    fun generateRandomNumber(number: Int) {
        _randomNumber.value = secureRandom.nextInt(number)
    }

    fun setCurrentItem(index: Int) {
        viewModelScope.launch {
            _currentItem.value = repository.getItem(index)
        }
    }
}