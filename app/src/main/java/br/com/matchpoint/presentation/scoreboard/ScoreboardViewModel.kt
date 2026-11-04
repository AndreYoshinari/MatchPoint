package br.com.matchpoint.presentation.scoreboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.usecase.FinishMatchUseCase
import br.com.matchpoint.domain.usecase.ObserveMatchUseCase
import br.com.matchpoint.domain.usecase.RegisterPointUseCase
import br.com.matchpoint.domain.usecase.StartMatchUseCase
import br.com.matchpoint.domain.usecase.UndoLastPointUseCase
import br.com.matchpoint.presentation.error.toUiMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScoreboardViewModel(
    savedStateHandle: SavedStateHandle,
    private val observeMatch: ObserveMatchUseCase,
    private val startMatch: StartMatchUseCase,
    private val registerPoint: RegisterPointUseCase,
    private val undoLastPoint: UndoLastPointUseCase,
    private val finishMatch: FinishMatchUseCase,
) : ViewModel() {

    private val matchId: String? = savedStateHandle[MATCH_ID_KEY]
    private var startRequested = false

    private val _uiState = MutableStateFlow<ScoreboardUiState>(ScoreboardUiState.Loading)
    val uiState: StateFlow<ScoreboardUiState> = _uiState.asStateFlow()

    init {
        val id = matchId
        if (id.isNullOrBlank()) {
            _uiState.value = ScoreboardUiState.Error("Partida não encontrada.")
        } else {
            viewModelScope.launch {
                observeMatch(id).collect { match ->
                    if (match?.status == MatchStatus.SETUP && !startRequested) {
                        startRequested = true
                        startMatchIfNeeded(id, match.home.id)
                        return@collect
                    }
                    if (match?.status == MatchStatus.SETUP) return@collect
                    _uiState.value = match.toScoreboardState()
                }
            }
        }
    }

    fun onPlayerPoint(playerId: String) {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = registerPoint(id, playerId)) {
                is DomainResult.Success -> Unit
                is DomainResult.Failure -> setActionError(result.error.toUiMessage())
            }
        }
    }

    fun onUndoLastPoint() {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = undoLastPoint(id)) {
                is DomainResult.Success -> Unit
                is DomainResult.Failure -> setActionError(result.error.toUiMessage())
            }
        }
    }

    fun onFinishMatch() {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = finishMatch(id)) {
                is DomainResult.Success -> Unit
                is DomainResult.Failure -> setActionError(result.error.toUiMessage())
            }
        }
    }

    private suspend fun startMatchIfNeeded(matchId: String, servingTeamId: String) {
        when (val result = startMatch(matchId, servingTeamId)) {
            is DomainResult.Success -> Unit
            is DomainResult.Failure -> {
                _uiState.value = ScoreboardUiState.Error(result.error.toUiMessage())
            }
        }
    }

    private fun setActionError(message: String) {
        _uiState.update { state ->
            when (state) {
                is ScoreboardUiState.Active -> state.copy(actionError = message)
                is ScoreboardUiState.Finished -> state.copy(actionError = message)
                else -> ScoreboardUiState.Error(message)
            }
        }
    }

    private fun Match?.toScoreboardState(): ScoreboardUiState {
        return when {
            this == null -> ScoreboardUiState.Error("Partida não encontrada.")
            status == MatchStatus.FINISHED -> ScoreboardUiState.Finished(this)
            status == MatchStatus.IN_PROGRESS -> ScoreboardUiState.Active(this)
            status == MatchStatus.SETUP -> ScoreboardUiState.Loading
            else -> ScoreboardUiState.Error("A partida ainda não foi iniciada.")
        }
    }

    companion object {
        const val MATCH_ID_KEY = "matchId"
    }
}
