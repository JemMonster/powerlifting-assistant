package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.data.api.NutritionEntryDto
import com.powerlifting.assistant.data.api.NutritionTodayResponse
import com.powerlifting.assistant.data.repo.NutritionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CaloriesViewModel @Inject constructor(
    private val nutritionRepository: NutritionRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val data: NutritionTodayResponse? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val data = nutritionRepository.getToday()
                _state.update { it.copy(loading = false, data = data) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun updateGoals(caloriesGoal: Int, proteinGoalG: Int) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                nutritionRepository.updateGoals(caloriesGoal, proteinGoalG)
                val data = nutritionRepository.getToday()
                _state.update { it.copy(loading = false, data = data) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun addEntry(title: String, calories: Int, proteinG: Int) {
        viewModelScope.launch {
            try {
                nutritionRepository.addEntry(title, calories, proteinG)
                val data = nutritionRepository.getToday()
                _state.update { it.copy(data = data) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = errorMessage(t)) }
            }
        }
    }

    fun deleteEntry(entry: NutritionEntryDto) {
        viewModelScope.launch {
            try {
                nutritionRepository.deleteEntry(entry.id)
                val data = nutritionRepository.getToday()
                _state.update { it.copy(data = data) }
            } catch (t: Throwable) {
                _state.update { it.copy(error = errorMessage(t)) }
            }
        }
    }
}
