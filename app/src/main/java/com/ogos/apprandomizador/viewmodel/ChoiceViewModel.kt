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

class ChoiceViewModel(repository: ItemListRepository) : ViewModel() {
    private val _randomNumber = MutableStateFlow(-1)
    val randomNumber: StateFlow<Int> = _randomNumber.asStateFlow()
    val items: StateFlow<List<ItemList>> =
        repository.getAllItems().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun generateRandomNumber(number: Int) {
        val secureRandom = SecureRandom()
        _randomNumber.value = secureRandom.nextInt(number)
    }

}