package com.ogos.apprandomizador.modes.domain

import kotlinx.coroutines.flow.Flow

data class AiConfig(val baseUrl: String, val model: String, val enabled: Boolean = true)

data class AiSettings(val config: AiConfig? = null, val hasApiKey: Boolean = false, val consentedAuthority: String? = null)

@JvmInline
value class Secret(val value: String) {
    override fun toString(): String = "Secret(••••••••)"
}

interface SecretStore {
    fun encrypt(secret: String): String
    fun decrypt(ciphertext: String): String?
}

interface AiSettingsRepository {
    val settings: Flow<AiSettings>
    suspend fun save(config: AiConfig, apiKey: String?)
    suspend fun removeCredentials()
    suspend fun apiKey(): String?
    suspend fun saveConsent(authority: String)
}

/** Pronto para aprofundar com IA: configurado, ativo, com chave e com consentimento para este host. */
fun AiSettings.isAiReady(): Boolean {
    val cfg = config ?: return false
    return cfg.enabled && hasApiKey && runCatching {
        consentedAuthority == BaseUrlNormalizer.authority(cfg.baseUrl)
    }.getOrDefault(false)
}
