package com.ogos.apprandomizador.modes.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ogos.apprandomizador.modes.domain.AiConfig
import com.ogos.apprandomizador.modes.domain.AiSettings
import com.ogos.apprandomizador.modes.domain.AiSettingsRepository
import com.ogos.apprandomizador.modes.domain.SecretStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.aiSettingsStore by preferencesDataStore(name = "ai_settings")

class AiSettingsDataStore(private val context: Context) {
    private val store = context.aiSettingsStore
    val values: Flow<AiSettings> = store.data.map { prefs ->
        val base = prefs[BASE_URL].orEmpty()
        val model = prefs[MODEL].orEmpty()
        val encrypted = prefs[ENCRYPTED_KEY]
        AiSettings(
            config = if (base.isNotBlank() && model.isNotBlank()) AiConfig(base, model, prefs[ENABLED] ?: true) else null,
            hasApiKey = !encrypted.isNullOrBlank(),
            consentedAuthority = prefs[CONSENT_AUTHORITY]
        )
    }

    suspend fun save(config: AiConfig, encryptedKey: String?) {
        store.edit { prefs ->
            prefs[BASE_URL] = config.baseUrl
            prefs[MODEL] = config.model
            prefs[ENABLED] = config.enabled
            if (!encryptedKey.isNullOrBlank()) prefs[ENCRYPTED_KEY] = encryptedKey
        }
    }

    suspend fun encryptedKey(): String? = store.data.first()[ENCRYPTED_KEY]
    suspend fun clearEncryptedKey() { store.edit { it.remove(ENCRYPTED_KEY) } }
    suspend fun saveConsent(authority: String) { store.edit { it[CONSENT_AUTHORITY] = authority } }
    suspend fun remove() { store.edit { it.clear() } }

    private companion object {
        val BASE_URL = stringPreferencesKey("base_url")
        val MODEL = stringPreferencesKey("model")
        val ENABLED = booleanPreferencesKey("enabled")
        val ENCRYPTED_KEY = stringPreferencesKey("encrypted_api_key")
        val CONSENT_AUTHORITY = stringPreferencesKey("consent_authority")
    }
}

class AiSettingsRepositoryImpl(
    private val dataStore: AiSettingsDataStore,
    private val secretStore: SecretStore
) : AiSettingsRepository {
    override val settings: Flow<AiSettings> = dataStore.values

    override suspend fun save(config: AiConfig, apiKey: String?) {
        val normalized = AiConfig(com.ogos.apprandomizador.modes.domain.BaseUrlNormalizer.normalize(config.baseUrl), config.model.trim(), config.enabled)
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
