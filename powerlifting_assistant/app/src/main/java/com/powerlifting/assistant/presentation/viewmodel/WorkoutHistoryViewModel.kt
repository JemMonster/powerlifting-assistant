package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.data.api.WorkoutHistoryItemDto
import com.powerlifting.assistant.data.repo.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class WorkoutHistoryViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val sessions: List<WorkoutHistoryItemDto> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val history = workoutRepository.getHistory()
                _state.update { it.copy(loading = false, sessions = history.sessions) }
            } catch (e: IOException) {
                _state.update { it.copy(loading = false, error = "Нет связи с сервером") }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }
}
