package com.ogos.apprandomizador.core.ai.data

import com.ogos.apprandomizador.core.ai.domain.AiConfig
import com.ogos.apprandomizador.core.ai.domain.AiSettings
import com.ogos.apprandomizador.core.ai.domain.AiSettingsRepository
import com.ogos.apprandomizador.core.ai.domain.BaseUrlNormalizer
import com.ogos.apprandomizador.core.ai.domain.SecretStore
import kotlinx.coroutines.flow.Flow

class AiSettingsRepositoryImpl(
    private val dataStore: AiSettingsDataStore,
    private val secretStore: SecretStore
) : AiSettingsRepository {
    override val settings: Flow<AiSettings> = dataStore.values

    override suspend fun save(config: AiConfig, apiKey: String?) {
        val normalized = AiConfig(BaseUrlNormalizer.normalize(config.baseUrl), config.model.trim(), config.enabled)
        require(normalized.model.isNotBlank()) { "Informe o model ID." }
        val encrypted = apiKey?.takeIf(String::isNotBlank)?.let(secretStore::encrypt)
        dataStore.save(normalized, encrypted)
    }

    override suspend fun removeCredentials() = dataStore.remove()

    override suspend fun saveConsent(authority: String) {
        require(authority.isNotBlank())
        dataStore.saveConsent(authority)
    }

    override suspend fun apiKey(): String? {
        val encrypted = dataStore.encryptedKey() ?: return null
        val decrypted = secretStore.decrypt(encrypted)
        if (decrypted == null) dataStore.clearEncryptedKey()
        return decrypted
    }
}
