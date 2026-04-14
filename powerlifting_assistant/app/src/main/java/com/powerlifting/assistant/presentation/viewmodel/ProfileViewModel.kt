package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.data.api.ProfileResponse
import com.powerlifting.assistant.data.api.UpdateProfileRequest
import com.powerlifting.assistant.data.repo.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val profile: ProfileResponse? = null,
        val saved: Boolean = false
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, saved = false) }
            try {
                val profile = profileRepository.getProfile()
                _state.update { it.copy(loading = false, profile = profile) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }

    fun save(req: UpdateProfileRequest) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null, saved = false) }
            try {
                profileRepository.updateProfile(req)
                val profile = profileRepository.getProfile()
                _state.update { it.copy(loading = false, profile = profile, saved = true) }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }
}
