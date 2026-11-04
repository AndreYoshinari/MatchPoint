package br.com.matchpoint.presentation.roster

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.usecase.AddPlayerUseCase
import br.com.matchpoint.domain.usecase.ObserveMatchUseCase
import br.com.matchpoint.domain.usecase.RemovePlayerUseCase
import br.com.matchpoint.domain.usecase.StartMatchUseCase
import br.com.matchpoint.presentation.error.toUiMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RosterViewModel(
    savedStateHandle: SavedStateHandle,
    private val observeMatch: ObserveMatchUseCase,
    private val addPlayer: AddPlayerUseCase,
    private val removePlayer: RemovePlayerUseCase,
    private val startMatch: StartMatchUseCase,
) : ViewModel() {

    private val matchId: String? = savedStateHandle[MATCH_ID_KEY]

    private val _uiState = MutableStateFlow<RosterUiState>(RosterUiState.Loading)
    val uiState: StateFlow<RosterUiState> = _uiState.asStateFlow()

    private val _events = Channel<RosterEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        val id = matchId
        if (id.isNullOrBlank()) {
            _uiState.value = RosterUiState.Error("Partida não encontrada.")
        } else {
            viewModelScope.launch {
                observeMatch(id).collect { match ->
                    _uiState.value = if (match == null) {
                        RosterUiState.Error("Partida não encontrada.")
                    } else {
                        val currentServing = (_uiState.value as? RosterUiState.Ready)?.servingTeamId
                        RosterUiState.Ready(
                            match = match,
                            servingTeamId = currentServing ?: match.home.id,
                        )
                    }
                }
            }
        }
    }

    fun onServingTeamSelected(teamId: String) {
        _uiState.update { state ->
            if (state is RosterUiState.Ready) {
                state.copy(servingTeamId = teamId, actionError = null)
            } else {
                state
            }
        }
    }

    fun onAddPlayer(teamId: String, name: String, number: Int? = null) {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = addPlayer(id, teamId, name, number)) {
                is DomainResult.Success -> Unit
                is DomainResult.Failure -> setActionError(result.error.toUiMessage())
            }
        }
    }

    fun onRemovePlayer(playerId: String) {
        val id = matchId ?: return
        viewModelScope.launch {
            when (val result = removePlayer(id, playerId)) {
                is DomainResult.Success -> Unit
                is DomainResult.Failure -> setActionError(result.error.toUiMessage())
            }
        }
    }

    fun onStartMatch() {
        val id = matchId ?: return
        val servingTeamId = (_uiState.value as? RosterUiState.Ready)?.servingTeamId ?: return
        viewModelScope.launch {
            when (val result = startMatch(id, servingTeamId)) {
                is DomainResult.Success -> _events.send(RosterEvent.MatchStarted(result.data.id))
                is DomainResult.Failure -> setActionError(result.error.toUiMessage())
            }
        }
    }

    private fun setActionError(message: String) {
        _uiState.update { state ->
            if (state is RosterUiState.Ready) {
                state.copy(actionError = message)
            } else {
                RosterUiState.Error(message)
            }
        }
    }

    companion object {
        const val MATCH_ID_KEY = "matchId"
    }
}
