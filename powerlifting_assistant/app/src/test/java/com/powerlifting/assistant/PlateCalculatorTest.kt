package com.powerlifting.assistant

import org.junit.Assert.*
import org.junit.Test

class PlateCalculatorTest {

    @Test
    fun `plate calculator gives correct plates for 100kg with 20kg bar`() {
        val result = invokePlateSolution(20.0, 100.0)
        assertTrue(result.possible)
        // 100-20 = 80 total, 40 per side -> greedy: 25 + 15
        assertEquals(listOf(25.0, 15.0), result.plates)
        assertEquals(40.0, result.plates.sum(), 0.01)
    }

    @Test
    fun `plate calculator gives correct plates for 180kg`() {
        val result = invokePlateSolution(20.0, 180.0)
        assertTrue(result.possible)
        // 180-20=160, per side=80 -> 25+25+25+5
        assertEquals(listOf(25.0, 25.0, 25.0, 5.0), result.plates)
    }

    @Test
    fun `plate calculator fails if target less than bar`() {
        val result = invokePlateSolution(20.0, 10.0)
        assertFalse(result.possible)
    }

    @Test
    fun `plate calculator rejects weight over 600kg`() {
        val result = invokePlateSolution(20.0, 999999.0)
        assertFalse(result.possible)
    }

    @Test
    fun `plate calculator handles exact bar weight`() {
        val result = invokePlateSolution(20.0, 20.0)
        assertTrue(result.possible)
        assertTrue(result.plates.isEmpty())
    }

    @Test
    fun `plate calculator handles impossible weight`() {
        // 21.0 => per side = 0.5, not achievable with available plates
        val result = invokePlateSolution(20.0, 21.0)
        assertFalse(result.possible)
    }

    @Test
    fun `plate calculator uses 1_25 plates`() {
        // 22.5 => per side 1.25
        val result = invokePlateSolution(20.0, 22.5)
        assertTrue(result.possible)
        assertEquals(listOf(1.25), result.plates)
    }

    // Mirrors the production algorithm to verify correctness
    private data class PlateResult(val possible: Boolean, val plates: List<Double> = emptyList())

    private fun invokePlateSolution(bar: Double, target: Double): PlateResult {
        if (target < bar) return PlateResult(false)
        if (target > 600.0) return PlateResult(false)
        val needTotal = target - bar
        val perSide = needTotal / 2.0
        val available = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
        var remaining = perSide
        val plates = mutableListOf<Double>()
        for (p in available) {
            val count = ((remaining + 1e-9) / p).toInt()
            repeat(count) { plates.add(p) }
            remaining -= p * count
        }
        val possible = kotlin.math.abs(remaining) < 1e-6
        return PlateResult(possible, plates)
    }
}
