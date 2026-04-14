package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.data.api.ProgramExerciseDto
import com.powerlifting.assistant.data.api.WorkoutSetDto
import com.powerlifting.assistant.data.repo.ProfileRepository
import com.powerlifting.assistant.data.repo.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

enum class WorkoutPhase {
    LOADING, WARMUP, EXERCISE, REST, ACCESSORIES, FINISH_RATING, FINISHED
}

data class ExerciseGroup(
    val name: String,
    val liftType: String,
    val setGroups: List<SetGroupInfo>,
    val isMain: Boolean
)

data class SetGroupInfo(
    val percent1rm: Double?,
    val targetReps: Int,
    val targetSets: Int,
    val weightKg: Double?,
    val completedSets: Int = 0
) {
    val totalSets: Int get() = targetSets
    val allCompleted: Boolean get() = completedSets >= totalSets
}

data class WorkoutUiState(
    val phase: WorkoutPhase = WorkoutPhase.LOADING,
    val error: String? = null,
    val recommendation: String? = null,

    // Exercise data
    val mainExercises: List<ExerciseGroup> = emptyList(),
    val accessoryExercises: List<ExerciseGroup> = emptyList(),
    val currentExerciseIndex: Int = 0,
    val currentSetGroupIndex: Int = 0,

    // Timers
    val exerciseTimerSec: Int = 0,
    val restTimerSec: Int = 0,
    val totalTimerSec: Int = 0,
    val isExerciseTimerRunning: Boolean = false,

    // Set tracking
    val completedSetsList: List<WorkoutSetDto> = emptyList(),

    // Finishing
    val wellbeingRating: Int = 3,
    val isFinishing: Boolean = false,

    // 1RM values for weight calculation
    val bench1rm: Double = 0.0,
    val squat1rm: Double = 0.0,
    val deadlift1rm: Double = 0.0
) {
    val currentExercise: ExerciseGroup?
        get() = mainExercises.getOrNull(currentExerciseIndex)

    val currentSetGroup: SetGroupInfo?
        get() = currentExercise?.setGroups?.getOrNull(currentSetGroupIndex)

    val totalMainSets: Int
        get() = mainExercises.sumOf { ex -> ex.setGroups.sumOf { it.totalSets } }

    val completedMainSets: Int
        get() = mainExercises.sumOf { ex -> ex.setGroups.sumOf { it.completedSets } }

    val allMainExercisesDone: Boolean
        get() = currentExerciseIndex >= mainExercises.size
}

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutUiState())
    val state: StateFlow<WorkoutUiState> = _state

    private var timerJob: Job? = null
    private var totalTimerJob: Job? = null
    private var sessionId: String = ""

    fun loadWorkout(sessionId: String) {
        this.sessionId = sessionId
        viewModelScope.launch {
            _state.update { it.copy(phase = WorkoutPhase.LOADING, error = null) }
            try {
                val profileResp = profileRepository.getProfile()
                val bench = profileResp.profile.bench1rm ?: 0.0
                val squat = profileResp.profile.squat1rm ?: 0.0
                val deadlift = profileResp.profile.deadlift1rm ?: 0.0

                val detail = workoutRepository.getSessionDetail(sessionId)

                val exerciseGroups = groupExercises(detail.exercises, bench, squat, deadlift)
                val main = exerciseGroups.filter { it.isMain }
                val accessory = exerciseGroups.filter { !it.isMain }

                _state.update {
                    it.copy(
                        phase = WorkoutPhase.WARMUP,
                        recommendation = detail.recommendation,
                        mainExercises = main,
                        accessoryExercises = accessory,
                        bench1rm = bench,
                        squat1rm = squat,
                        deadlift1rm = deadlift
                    )
                }

                startTotalTimer()
            } catch (e: IOException) {
                _state.update { it.copy(phase = WorkoutPhase.LOADING, error = "Нет связи с сервером") }
            } catch (t: Throwable) {
                _state.update { it.copy(phase = WorkoutPhase.LOADING, error = t.message ?: "Ошибка загрузки") }
            }
        }
    }

    fun warmupDone() {
        if (_state.value.mainExercises.isEmpty()) {
            _state.update { it.copy(phase = WorkoutPhase.ACCESSORIES) }
        } else {
            _state.update { it.copy(phase = WorkoutPhase.EXERCISE, currentExerciseIndex = 0, currentSetGroupIndex = 0) }
        }
    }

    fun startSet() {
        _state.update { it.copy(isExerciseTimerRunning = true, exerciseTimerSec = 0) }
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(exerciseTimerSec = it.exerciseTimerSec + 1) }
            }
        }
    }

    fun completeSet() {
        timerJob?.cancel()

        val s = _state.value
        val exercise = s.currentExercise ?: return
        val setGroup = s.currentSetGroup ?: return

        // Record the completed set
        val setNumber = s.completedSetsList.size + 1
        val weight = setGroup.weightKg ?: 0.0
        val loggedSet = WorkoutSetDto(
            exerciseName = exercise.name,
            setNumber = setNumber,
            weightKg = weight,
            reps = setGroup.targetReps,
            rpe = null
        )

        val updatedSets = s.completedSetsList + loggedSet

        // Update set completion count
        val updatedExercises = s.mainExercises.toMutableList()
        val ex = updatedExercises[s.currentExerciseIndex]
        val updatedSetGroups = ex.setGroups.toMutableList()
        updatedSetGroups[s.currentSetGroupIndex] = setGroup.copy(completedSets = setGroup.completedSets + 1)
        updatedExercises[s.currentExerciseIndex] = ex.copy(setGroups = updatedSetGroups)

        _state.update {
            it.copy(
                mainExercises = updatedExercises,
                completedSetsList = updatedSets,
                isExerciseTimerRunning = false,
                exerciseTimerSec = 0,
                restTimerSec = 0,
                phase = WorkoutPhase.REST
            )
        }

        // Start rest timer
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(restTimerSec = it.restTimerSec + 1) }
            }
        }
    }

    fun skipRest() {
        timerJob?.cancel()
        advanceToNextSet()
    }

    private fun advanceToNextSet() {
        val s = _state.value
        val exercise = s.currentExercise ?: return
        val currentSg = exercise.setGroups.getOrNull(s.currentSetGroupIndex)

        if (currentSg != null && !currentSg.allCompleted) {
            // More sets in current set group
            _state.update { it.copy(phase = WorkoutPhase.EXERCISE) }
            return
        }

        // Move to next set group within same exercise
        val nextSgIndex = s.currentSetGroupIndex + 1
        if (nextSgIndex < exercise.setGroups.size) {
            _state.update { it.copy(currentSetGroupIndex = nextSgIndex, phase = WorkoutPhase.EXERCISE) }
            return
        }

        // Move to next exercise
        val nextExIndex = s.currentExerciseIndex + 1
        if (nextExIndex < s.mainExercises.size) {
            _state.update { it.copy(currentExerciseIndex = nextExIndex, currentSetGroupIndex = 0, phase = WorkoutPhase.EXERCISE) }
            return
        }

        // All main exercises done
        _state.update { it.copy(phase = WorkoutPhase.ACCESSORIES) }
    }

    fun proceedToFinish() {
        _state.update { it.copy(phase = WorkoutPhase.FINISH_RATING) }
    }

    fun setWellbeingRating(rating: Int) {
        _state.update { it.copy(wellbeingRating = rating) }
    }

    fun finishWorkout() {
        viewModelScope.launch {
            _state.update { it.copy(isFinishing = true, error = null) }
            try {
                // Send completed sets
                val sets = _state.value.completedSetsList
                if (sets.isNotEmpty()) {
                    workoutRepository.addSets(sessionId, sets)
                }

                // Finish session with rating
                workoutRepository.finishSession(
                    sessionId,
                    _state.value.totalTimerSec,
                    _state.value.wellbeingRating
                )

                totalTimerJob?.cancel()
                _state.update { it.copy(isFinishing = false, phase = WorkoutPhase.FINISHED) }
            } catch (e: IOException) {
                _state.update { it.copy(isFinishing = false, error = "Нет связи с сервером") }
            } catch (t: Throwable) {
                _state.update { it.copy(isFinishing = false, error = t.message ?: "Ошибка") }
            }
        }
    }

    private fun startTotalTimer() {
        totalTimerJob?.cancel()
        totalTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _state.update { it.copy(totalTimerSec = it.totalTimerSec + 1) }
            }
        }
    }

    private fun groupExercises(
        exercises: List<ProgramExerciseDto>,
        bench1rm: Double,
        squat1rm: Double,
        deadlift1rm: Double
    ): List<ExerciseGroup> {
        val grouped = exercises.groupBy { it.exerciseName }

        return grouped.map { (name, exList) ->
            val liftType = exList.first().liftType
            val isMain = liftType in listOf("squat", "bench", "deadlift")

            val rm = when (liftType) {
                "squat" -> squat1rm
                "bench" -> bench1rm
                "deadlift" -> deadlift1rm
                else -> 0.0
            }

            val setGroups = exList.sortedBy { it.orderIndex }.map { ex ->
                val reps = ex.reps.toIntOrNull() ?: ex.reps.split("-").firstOrNull()?.toIntOrNull() ?: 8
                val weight = if (ex.percent1rm != null && rm > 0) {
                    roundToPlate(rm * ex.percent1rm)
                } else null

                SetGroupInfo(
                    percent1rm = ex.percent1rm,
                    targetReps = reps,
                    targetSets = ex.sets,
                    weightKg = weight
                )
            }

            ExerciseGroup(
                name = name,
                liftType = liftType,
                isMain = isMain,
                setGroups = setGroups
            )
        }
    }

    private fun roundToPlate(kg: Double): Double {
        return Math.round(kg / 2.5) * 2.5
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        totalTimerJob?.cancel()
    }
}
