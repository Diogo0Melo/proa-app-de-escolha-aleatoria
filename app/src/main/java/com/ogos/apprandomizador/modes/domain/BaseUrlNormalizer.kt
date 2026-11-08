package com.ogos.apprandomizador.modes.domain

object BaseUrlNormalizer {
    fun normalize(value: String): String {
        val trimmed = value.trim().removeSuffix("/").removeSuffix("/chat/completions").removeSuffix("/")
        val uri = runCatching { java.net.URI(trimmed) }.getOrNull()
        require(uri != null && uri.scheme.equals("https", true) && !uri.host.isNullOrBlank()) {
            "Só endereços HTTPS são aceitos."
        }
        require(uri.userInfo == null && uri.query == null && uri.fragment == null) { "Informe apenas a URL base HTTPS." }
        return trimmed
    }

    fun authority(value: String): String {
        val normalized = normalize(value)
        val uri = java.net.URI(normalized)
        return uri.host.lowercase() + if (uri.port >= 0) ":${uri.port}" else ""
    }
}
