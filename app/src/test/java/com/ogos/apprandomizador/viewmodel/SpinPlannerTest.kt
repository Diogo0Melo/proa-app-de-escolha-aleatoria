package com.ogos.apprandomizador.viewmodel

import com.ogos.apprandomizador.model.RaffleItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpinPlannerTest {
    private val seed = ByteArray(32) { it.toByte() }
    private val items = (0 until 100).map { RaffleItem("Item $it", it.toLong()) }

    @Test
    fun `golden output preserves winner cosmetics and shuffle seed`() {
        val expected = mapOf(
            5 to 1,
            24 to 19,
            100 to 91
        )
        expected.forEach { (size, winnerIndex) ->
            val target = planSpin(items.take(size), 42L, seed, 1L) { 0 }
            assertEquals("Item $winnerIndex", target.targetItem.name)
            assertEquals(42L, target.listId)
            assertEquals(1L, target.requestId)
            assertEquals(4.72955E-14f, target.cosmetics.travelUnit, 1.0E-20f)
            assertEquals(1.6020518E-13f, target.cosmetics.overshootUnit, 1.0E-20f)
            assertEquals(1.120215E-13f, target.cosmetics.hesitationAmplitudeUnit, 1.0E-20f)
            assertEquals(7.7826634E-14f, target.cosmetics.hesitationCyclesUnit, 1.0E-20f)
            assertEquals(8.9928065E-14f, target.cosmetics.durationUnit, 1.0E-20f)
            assertEquals(318698951L, target.shuffleSeed)
        }
    }

    @Test
    fun `one item always wins and invalid empty list is rejected`() {
        val target = planSpin(items.take(1), 1L, seed, 1L) { 0 }
        assertEquals("Item 0", target.targetItem.name)
        try {
            planSpin(emptyList(), 1L, seed, 1L) { 0 }
            throw AssertionError("empty list should be rejected")
        } catch (_: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `stream consumption order is winner then five cosmetics then shuffle seed`() {
        val stream = deriveSpinStream(seed, 1L)
        val winnerIndex = pickWinnerIndex(stream, 24) { 0 }
        val expectedUnits = List(5) { stream.nextDouble().toFloat() }
        val expectedShuffle = stream.nextIntNonNegative().toLong() and 0xFFFFFFFFL
        val target = planSpin(items.take(24), 9L, seed, 1L) { 0 }
        assertEquals("Item $winnerIndex", target.targetItem.name)
        assertEquals(
            expectedUnits, listOf(
                target.cosmetics.travelUnit,
                target.cosmetics.overshootUnit,
                target.cosmetics.hesitationAmplitudeUnit,
                target.cosmetics.hesitationCyclesUnit,
                target.cosmetics.durationUnit
            )
        )
        assertEquals(expectedShuffle, target.shuffleSeed)
    }

    @Test
    fun `request id changes cosmetics`() {
        val first = planSpin(items.take(24), 1L, seed, 1L) { 0 }
        val second = planSpin(items.take(24), 1L, seed, 2L) { 0 }
        assertTrue(first.cosmetics != second.cosmetics || first.shuffleSeed != second.shuffleSeed)
    }
}
