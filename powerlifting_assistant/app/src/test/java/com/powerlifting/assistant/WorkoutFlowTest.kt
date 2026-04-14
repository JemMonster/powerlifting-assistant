package com.powerlifting.assistant

import com.powerlifting.assistant.presentation.viewmodel.ExerciseGroup
import com.powerlifting.assistant.presentation.viewmodel.SetGroupInfo
import com.powerlifting.assistant.presentation.viewmodel.WorkoutUiState
import org.junit.Assert.*
import org.junit.Test

class WorkoutFlowTest {

    @Test
    fun `exercise group tracks completed sets correctly`() {
        val setGroup = SetGroupInfo(
            percent1rm = 0.70,
            targetReps = 5,
            targetSets = 3,
            weightKg = 100.0,
            completedSets = 0
        )

        assertFalse(setGroup.allCompleted)
        assertEquals(3, setGroup.totalSets)

        val afterOne = setGroup.copy(completedSets = 1)
        assertFalse(afterOne.allCompleted)

        val afterAll = setGroup.copy(completedSets = 3)
        assertTrue(afterAll.allCompleted)
    }

    @Test
    fun `workout state tracks progress correctly`() {
        val exercises = listOf(
            ExerciseGroup(
                name = "Присед",
                liftType = "squat",
                isMain = true,
                setGroups = listOf(
                    SetGroupInfo(0.65, 5, 3, 97.5),
                    SetGroupInfo(0.75, 3, 2, 112.5),
                )
            ),
            ExerciseGroup(
                name = "Жим лёжа",
                liftType = "bench",
                isMain = true,
                setGroups = listOf(
                    SetGroupInfo(0.65, 5, 3, 65.0),
                )
            )
        )

        val state = WorkoutUiState(mainExercises = exercises)

        // Total sets = 3 + 2 + 3 = 8
        assertEquals(8, state.totalMainSets)
        assertEquals(0, state.completedMainSets)
        assertFalse(state.allMainExercisesDone)

        assertEquals("Присед", state.currentExercise?.name)
        assertEquals(0.65, state.currentSetGroup?.percent1rm ?: 0.0, 0.01)
    }

    @Test
    fun `accessory exercises are not main`() {
        val accessory = ExerciseGroup(
            name = "Тяга в наклоне",
            liftType = "other",
            isMain = false,
            setGroups = listOf(SetGroupInfo(null, 10, 4, null))
        )

        assertFalse(accessory.isMain)
        assertNull(accessory.setGroups.first().percent1rm)
    }

    @Test
    fun `roundToPlate rounds to nearest 2_5`() {
        // Test the rounding logic used in ViewModel
        assertEquals(100.0, roundToPlate(101.0), 0.01)
        assertEquals(102.5, roundToPlate(103.0), 0.01)
        assertEquals(100.0, roundToPlate(100.0), 0.01)
        assertEquals(97.5, roundToPlate(98.0), 0.01)
        assertEquals(0.0, roundToPlate(1.0), 0.01)
    }

    private fun roundToPlate(kg: Double): Double {
        return Math.round(kg / 2.5) * 2.5
    }
}
