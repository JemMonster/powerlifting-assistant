package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.data.api.AchievementDto
import com.powerlifting.assistant.data.repo.AchievementsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val repo: AchievementsRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val items: List<AchievementDto> = emptyList()
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val list = repo.list()
                _state.update { it.copy(loading = false, items = list) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun add(note: String) {
        viewModelScope.launch {
            try {
                repo.create(note)
                load()
            } catch (t: Throwable) {
                _state.update { it.copy(error = errorMessage(t)) }
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            try {
                repo.delete(id)
                load()
            } catch (t: Throwable) {
                _state.update { it.copy(error = errorMessage(t)) }
            }
        }
    }
}
