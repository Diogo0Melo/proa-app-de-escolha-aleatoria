package com.ogos.apprandomizador.core.ai.domain

interface SecretStore {
    fun encrypt(secret: String): String
    fun decrypt(ciphertext: String): String?
}
