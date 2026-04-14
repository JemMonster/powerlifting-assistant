package com.powerlifting.assistant.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerlifting.assistant.data.api.CalendarResponse
import com.powerlifting.assistant.data.api.ProfileResponse
import com.powerlifting.assistant.data.repo.ProfileRepository
import com.powerlifting.assistant.data.repo.ProgramRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val programRepository: ProgramRepository
) : ViewModel() {

    data class UiState(
        val loading: Boolean = false,
        val error: String? = null,
        val profile: ProfileResponse? = null,
        val calendar: CalendarResponse? = null
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            try {
                val now = LocalDate.now(ZoneId.systemDefault())
                val from = now.withDayOfMonth(1)
                val to = from.plusMonths(1).minusDays(1)

                val (profile, calendar) = coroutineScope {
                    val profileDeferred = async { profileRepository.getProfile() }
                    val calendarDeferred = async { programRepository.calendar(from.toString(), to.toString()) }
                    profileDeferred.await() to calendarDeferred.await()
                }

                _state.update { it.copy(loading = false, profile = profile, calendar = calendar) }
            } catch (e: IOException) {
                _state.update { it.copy(loading = false, error = "Нет связи с сервером. Проверьте подключение к интернету.") }
            } catch (t: Throwable) {
                _state.update { it.copy(loading = false, error = errorMessage(t)) }
            }
        }
    }
}

internal fun errorMessage(t: Throwable): String {
    val msg = t.message ?: ""
    return when {
        msg.contains("503") -> "Сервер временно недоступен. Повторите позже."
        msg.contains("Unable to resolve host") -> "Нет связи с сервером. Проверьте интернет."
        msg.contains("timeout", ignoreCase = true) -> "Время ожидания истекло. Повторите."
        msg.isNotBlank() -> msg
        else -> "Неизвестная ошибка"
    }
}
