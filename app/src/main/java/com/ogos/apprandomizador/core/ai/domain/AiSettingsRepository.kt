package com.ogos.apprandomizador.core.ai.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

interface AiSettingsRepository {
    val settings: Flow<AiSettings>
    suspend fun save(config: AiConfig, apiKey: String?)
    suspend fun removeCredentials()
    suspend fun apiKey(): String?
    suspend fun saveConsent(authority: String)
}

// A leitura das configurações é mantida num repositório de fluxo; este helper lê o valor atual sem expor a chave.
suspend fun AiSettingsRepository.settingsSnapshot(): AiConfig? = settings.first().let { state ->
    state.config?.takeIf { it.enabled && state.consentedAuthority == BaseUrlNormalizer.authority(it.baseUrl) }
}
