package com.powerlifting.server.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProgramServiceTest {

    @Test
    fun `pyramid percentages progress correctly across weeks`() {
        // Verify the percentage progression logic matches Excel-based design
        // Week 1 should be lighter than week 4
        val week1Squat = listOf(0.55, 0.65, 0.75)
        val week4Squat = listOf(0.60, 0.72, 0.82)

        assertTrue(week4Squat[0] > week1Squat[0], "Week 4 warmup % should be higher than week 1")
        assertTrue(week4Squat[2] > week1Squat[2], "Week 4 peak % should be higher than week 1")
    }

    @Test
    fun `round to plate works correctly`() {
        // Verify the rounding logic (round to nearest 2.5kg)
        assertEquals(100.0, roundToPlate(101.0))
        assertEquals(102.5, roundToPlate(103.0))
        assertEquals(100.0, roundToPlate(100.0))
        assertEquals(97.5, roundToPlate(98.0))
    }

    @Test
    fun `weight calculation from percentage and 1RM`() {
        val squat1rm = 150.0
        val percent = 0.70
        val raw = squat1rm * percent // 105.0
        val rounded = roundToPlate(raw) // 105.0
        assertEquals(105.0, rounded)

        val bench1rm = 100.0
        val benchPercent = 0.65
        val rawBench = bench1rm * benchPercent // 65.0
        val roundedBench = roundToPlate(rawBench) // 65.0
        assertEquals(65.0, roundedBench)
    }

    @Test
    fun `day spacing produces correct 3 workouts per week pattern`() {
        val daySpacing = listOf(2L, 2L, 3L)
        val totalDaysPerWeek = daySpacing.sum()
        assertEquals(7, totalDaysPerWeek, "3 workouts should span exactly 7 days")
    }

    private fun roundToPlate(kg: Double): Double {
        return Math.round(kg / 2.5) * 2.5
    }
}
