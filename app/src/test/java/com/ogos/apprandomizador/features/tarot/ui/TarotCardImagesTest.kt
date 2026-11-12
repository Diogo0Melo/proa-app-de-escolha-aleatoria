package com.ogos.apprandomizador.features.tarot.ui

import com.ogos.apprandomizador.features.tarot.data.FullTarotDeck
import java.io.File
import org.junit.Assert.*
import org.junit.Test

class TarotCardImagesTest {
    private val assetsRoot = File("src/main/assets")

    @Test fun `arcanos maiores usam o prefixo m`() {
        assertEquals("tarot/m00.webp", TarotCardImages.assetPathFor("00"))
        assertEquals("tarot/m21.webp", TarotCardImages.assetPathFor("21"))
    }

    @Test fun `arcanos menores usam o proprio id`() {
        assertEquals("tarot/w01.webp", TarotCardImages.assetPathFor("w01"))
        assertEquals("tarot/p14.webp", TarotCardImages.assetPathFor("p14"))
    }

    @Test fun `ids invalidos nao geram caminho`() {
        listOf("", "22", "23", "x01", "w00", "w15", "m00", "c1", "00 ", "../00").forEach { id ->
            assertNull("id '$id'", TarotCardImages.assetPathFor(id))
        }
    }

    @Test fun `cada carta do baralho completo tem caminho unico`() {
        val paths = FullTarotDeck.getDeck().map { TarotCardImages.assetPathFor(it.id) }
        assertTrue(paths.all { it != null })
        assertEquals(paths.size, paths.distinct().size)
    }

    @Test fun `cada carta do baralho tem imagem entregue no projeto`() {
        FullTarotDeck.getDeck().forEach { card ->
            val path = TarotCardImages.assetPathFor(card.id)!!
            assertTrue("falta $path", File(assetsRoot, path).isFile)
        }
    }

    @Test fun `verso da carta existe no projeto`() {
        assertTrue(File(assetsRoot, TarotCardImages.CARD_BACK_PATH).isFile)
    }

    @Test fun `uri usa o esquema de assets do Android`() {
        assertEquals("file:///android_asset/tarot/m00.webp", TarotCardImages.assetUri("tarot/m00.webp"))
    }
}
