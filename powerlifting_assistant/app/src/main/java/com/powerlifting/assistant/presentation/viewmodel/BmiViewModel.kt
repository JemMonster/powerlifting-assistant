package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.data.repo.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BmiViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val heightCm: Int? = null,
        val weightKg: Double? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val profile = profileRepository.getProfile()
                _state.update {
                    it.copy(
                        loading = false,
                        heightCm = profile.profile.heightCm,
                        weightKg = profile.profile.weightKg
                    )
                }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }
}
