package com.ogos.apprandomizador.core.ai.domain

data class AiSettings(val config: AiConfig? = null, val hasApiKey: Boolean = false, val consentedAuthority: String? = null)

/** Pronto para aprofundar com IA: configurado, ativo, com chave e com consentimento para este host. */
fun AiSettings.isAiReady(): Boolean {
    val cfg = config ?: return false
    return cfg.enabled && hasApiKey && runCatching {
        consentedAuthority == BaseUrlNormalizer.authority(cfg.baseUrl)
    }.getOrDefault(false)
}
