package com.ogos.apprandomizador.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.lifecycle.ViewModel
import java.security.SecureRandom

class ChoiceViewModel() : ViewModel() {
    private val _randomNumber = mutableIntStateOf(-1)
    val randomNumber: State<Int> = _randomNumber

    fun generateRandomNumber(number: Int) {
        val secureRandom = SecureRandom()
        _randomNumber.intValue = secureRandom.nextInt(number)
    }
}