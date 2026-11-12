package com.ogos.apprandomizador.core.ai.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class KeystoreSecretStoreTest {
    @Test fun encryptsAndDecryptsWithAndroidKeystore() {
        val store = KeystoreSecretStore()
        val encrypted = store.encrypt("test-api-key")
        assertNotEquals("test-api-key", encrypted)
        assertEquals("test-api-key", store.decrypt(encrypted))
    }

    @Test fun invalidCiphertextReturnsNull() {
        assertNull(KeystoreSecretStore().decrypt("not-valid-ciphertext"))
    }
}
