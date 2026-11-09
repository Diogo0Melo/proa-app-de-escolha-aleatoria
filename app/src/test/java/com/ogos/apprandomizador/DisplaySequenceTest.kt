package com.ogos.apprandomizador

import com.ogos.apprandomizador.model.RaffleItem
import com.ogos.apprandomizador.ui.view.buildDisplaySequence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DisplaySequenceTest {

    private fun items(count: Int): List<RaffleItem> =
        (1..count).map { RaffleItem("Item $it", 0xFF000000L or it.toLong()) }

    @Test
    fun emptyInputReturnsEmpty() {
        assertTrue(buildDisplaySequence(emptyList(), null).isEmpty())
    }

    @Test
    fun largeInputIsCappedAtMaxDisplaySlots() {
        val seq = buildDisplaySequence(items(100), null, maxDisplaySlots = 24)
        assertEquals(24, seq.size)
    }

    @Test
    fun targetAlwaysIncludedWhenPresent() {
        val all = items(100)
        val target = all[73]
        val seq = buildDisplaySequence(all, target, maxDisplaySlots = 24)
        assertTrue("target missing", seq.contains(target))
        assertEquals(24, seq.size)
    }

    @Test
    fun subsetHasNoDuplicates() {
        val all = items(60)
        val seq = buildDisplaySequence(all, all[5], maxDisplaySlots = 24)
        assertEquals(seq.size, seq.distinct().size)
    }

    @Test
    fun smallInputShowsEachItemOnce() {
        val all = items(10)
        val seq = buildDisplaySequence(all, null, maxDisplaySlots = 24)
        assertEquals(10, seq.size)
        assertEquals(all.map { it.name }.sorted(), seq.map { it.name }.sorted())
    }

    @Test
    fun tinyListIsCycledToAtLeastFourSlots() {
        for (n in listOf(1, 2, 3)) {
            val seq = buildDisplaySequence(items(n), null, maxDisplaySlots = 24)
            assertTrue("n=$n size=${seq.size}", seq.size >= 4)
        }
        val single = buildDisplaySequence(items(1), null, maxDisplaySlots = 24)
        assertEquals(4, single.size)
        assertTrue(single.all { it == single[0] })
    }

    @Test
    fun targetNotInListFallsBackToSubset() {
        val all = items(50)
        val stranger = RaffleItem("Ghost", 0xFF123456L)
        val seq = buildDisplaySequence(all, stranger, maxDisplaySlots = 24)
        assertEquals(24, seq.size)
        assertTrue(seq.none { it == stranger })
    }
}
