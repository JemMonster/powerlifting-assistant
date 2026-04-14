package com.powerlifting.server.service

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RecoveryServiceTest {

    private val service = RecoveryService()

    @Test
    fun `recommends postponing when sleep is very low`() {
        val r = service.makeRecommendation(sleepHours = 3.5, wellbeing = 7, fatigue = 5, soreness = 5)
        assertNotNull(r)
        assertTrue(r.contains("перенести", ignoreCase = true))
    }

    @Test
    fun `recommends reducing load when fatigue is high`() {
        val r = service.makeRecommendation(sleepHours = 7.0, wellbeing = 7, fatigue = 9, soreness = 5)
        assertNotNull(r)
        assertTrue(r.contains("снизить", ignoreCase = true) || r.contains("уменьш", ignoreCase = true))
    }

    @Test
    fun `allows training when metrics are ok`() {
        val r = service.makeRecommendation(sleepHours = 8.0, wellbeing = 8, fatigue = 5, soreness = 4)
        assertNotNull(r)
        assertTrue(r.contains("Можно", ignoreCase = true))
    }
}
