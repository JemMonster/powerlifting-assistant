package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.data.api.ProgramWorkoutDto
import com.powerlifting.assistant.data.api.StartWorkoutSessionRequest
import com.powerlifting.assistant.data.repo.ProgramRepository
import com.powerlifting.assistant.data.repo.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class RecoveryViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val plannedWorkout: ProgramWorkoutDto? = null,
        val recommendation: String? = null,
        val startedSessionId: String? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val active = programRepository.getActive()
                val today = LocalDate.now(ZoneId.systemDefault()).toString()
                val todayWorkout = active.upcomingWorkouts.firstOrNull { it.date == today }
                    ?: active.upcomingWorkouts.firstOrNull()

                _state.update { it.copy(loading = false, plannedWorkout = todayWorkout) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun startSession(
        sleepHours: Double,
        wellbeing: Int,
        fatigue: Int,
        soreness: Int
    ) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, recommendation = null, startedSessionId = null) }
            try {
                val workoutId = _state.value.plannedWorkout?.id
                val resp = workoutRepository.startSession(
                    StartWorkoutSessionRequest(
                        programWorkoutId = workoutId,
                        sleepHours = sleepHours,
                        wellbeing = wellbeing,
                        fatigue = fatigue,
                        soreness = soreness
                    )
                )
                _state.update { it.copy(loading = false, recommendation = resp.recommendation, startedSessionId = resp.sessionId) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }
}
