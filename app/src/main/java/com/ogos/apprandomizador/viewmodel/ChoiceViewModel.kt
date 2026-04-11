package com.ogos.apprandomizador.viewmodel

import androidx.lifecycle.ViewModel
import java.security.SecureRandom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChoiceViewModel : ViewModel() {
    private val _randomNumber = MutableStateFlow(-1)
    val randomNumber: StateFlow<Int> = _randomNumber.asStateFlow()

    fun generateRandomNumber(number: Int) {
        val secureRandom = SecureRandom()
        _randomNumber.value = secureRandom.nextInt(number)
    }
}