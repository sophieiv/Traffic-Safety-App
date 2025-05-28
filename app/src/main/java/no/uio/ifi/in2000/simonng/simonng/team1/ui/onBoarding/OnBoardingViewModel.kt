package no.uio.ifi.in2000.simonng.simonng.team1.ui.onBoarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.simonng.simonng.team1.data.onBoarding.OnBoardingRepository

data class OnboardingUiState(
    val index: Int = 0,
    val progressValue: Float = 0f,
    val showOnboarding: Boolean = true,
)

class OnBoardingViewModel(
    private val onboardingRepository: OnBoardingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val onboardingUiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            onboardingRepository.onboardingStatusFlow.collectLatest { visOnboarding ->
                _uiState.update { it.copy(showOnboarding = visOnboarding) }
            }
        }
    }

    fun updateShowOnboarding(state: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(showOnboarding = state) }
            onboardingRepository.setOnboardingCompleted(false)
        }
    }

    fun gaaTilForrigeSkjerm() {
        viewModelScope.launch {
            _uiState.update { it.copy(index = it.index - 1) }
            oppdaterProgresjonsVerdi()
        }
    }

    fun gaaTilNesteSkjerm() {
        viewModelScope.launch {
            _uiState.update { it.copy(index = it.index + 1) }
            oppdaterProgresjonsVerdi()
        }
    }

    private fun oppdaterProgresjonsVerdi() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(progressValue = _uiState.value.index.toFloat() / 4)
            }
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OnBoardingViewModel::class.java)) {
                return OnBoardingViewModel(OnBoardingRepository.getInstance(context)) as T
            }
            throw IllegalArgumentException("Ukjent ViewModel klasse")
        }
    }
}