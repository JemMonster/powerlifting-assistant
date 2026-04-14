package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.data.api.ActiveProgramResponse
import com.powerlifting.assistant.data.repo.ProfileRepository
import com.powerlifting.assistant.data.repo.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProgramViewModel @Inject constructor(
    private val programRepository: ProgramRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val active: ActiveProgramResponse? = null,
        val profileMissingMaxes: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val profile = profileRepository.getProfile()
                val maxMissing = profile.profile.bench1rm == null || profile.profile.squat1rm == null || profile.profile.deadlift1rm == null

                val active = programRepository.getActive()
                _state.update { it.copy(loading = false, active = active, profileMissingMaxes = maxMissing) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun generate() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                programRepository.generate()
                val active = programRepository.getActive()
                _state.update { it.copy(loading = false, active = active) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }
}
