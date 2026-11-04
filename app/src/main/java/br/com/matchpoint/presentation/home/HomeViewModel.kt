package br.com.matchpoint.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.usecase.ObserveMatchHistoryUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(
    private val observeMatchHistory: ObserveMatchHistoryUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observeHistory()
    }

    fun onRetry() {
        observeHistory()
    }

    private fun observeHistory() {
        observeJob?.cancel()
        _uiState.value = HomeUiState.Loading
        observeJob = viewModelScope.launch {
            observeMatchHistory()
                .catch {
                    _uiState.value = HomeUiState.Error("Não foi possível carregar as partidas.")
                }
                .collect { matches ->
                    _uiState.value = if (matches.isEmpty()) {
                        HomeUiState.Empty
                    } else {
                        HomeUiState.Content(matches.map { it.toHomeItem() })
                    }
                }
        }
    }

    private fun Match.toHomeItem(): HomeMatchItem {
        return HomeMatchItem(
            id = id,
            homeName = home.name,
            awayName = away.name,
            setsScore = "$homeSetsWon x $awaySetsWon",
            statusLabel = when (status) {
                MatchStatus.SETUP -> "Rascunho"
                MatchStatus.IN_PROGRESS -> "Em andamento"
                MatchStatus.FINISHED -> "Finalizada"
            },
        )
    }
}
