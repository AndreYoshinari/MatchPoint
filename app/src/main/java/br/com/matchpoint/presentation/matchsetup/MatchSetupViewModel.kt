package br.com.matchpoint.presentation.matchsetup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.usecase.AddPlayerUseCase
import br.com.matchpoint.domain.usecase.CreateMatchUseCase
import br.com.matchpoint.domain.usecase.ObserveMatchUseCase
import br.com.matchpoint.domain.usecase.UpdateMatchSetupUseCase
import br.com.matchpoint.presentation.error.toUiMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MatchSetupViewModel(
    savedStateHandle: SavedStateHandle,
    private val createMatch: CreateMatchUseCase,
    private val updateMatchSetup: UpdateMatchSetupUseCase,
    private val addPlayer: AddPlayerUseCase,
    private val observeMatch: ObserveMatchUseCase,
) : ViewModel() {

    private val matchId: String? = savedStateHandle[MATCH_ID_KEY]
    private var nextPlayerId = 0

    private val _uiState = MutableStateFlow(MatchSetupUiState(isLoading = matchId != null))
    val uiState: StateFlow<MatchSetupUiState> = _uiState.asStateFlow()

    private val _events = Channel<MatchSetupEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        val id = matchId
        if (id != null) {
            viewModelScope.launch {
                observeMatch(id).collect { match ->
                    if (match == null) {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "Partida não encontrada.")
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                homeName = match.home.name,
                                awayName = match.away.name,
                                setsToWin = match.settings.setsToWin,
                                pointsToWinSet = match.settings.pointsToWinSet,
                                pointsToWinLastSet = match.settings.pointsToWinLastSet,
                                mustWinByTwo = match.settings.mustWinByTwo,
                                homePlayers = match.playersOf(match.home.id).map { player ->
                                    SetupPlayerDraft(player.id, player.name)
                                },
                                awayPlayers = match.playersOf(match.away.id).map { player ->
                                    SetupPlayerDraft(player.id, player.name)
                                },
                                errorMessage = null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onHomeNameChange(value: String) {
        _uiState.update { it.copy(homeName = value, errorMessage = null) }
    }

    fun onAwayNameChange(value: String) {
        _uiState.update { it.copy(awayName = value, errorMessage = null) }
    }

    fun onSetsToWinChange(value: Int) {
        _uiState.update { it.copy(setsToWin = value, errorMessage = null) }
    }

    fun onHomePlayerDraftChange(value: String) {
        _uiState.update { it.copy(homePlayerDraft = value, errorMessage = null) }
    }

    fun onAwayPlayerDraftChange(value: String) {
        _uiState.update { it.copy(awayPlayerDraft = value, errorMessage = null) }
    }

    fun onAddHomePlayer() {
        addDraftPlayer(homeTeam = true)
    }

    fun onAddAwayPlayer() {
        addDraftPlayer(homeTeam = false)
    }

    fun onRemoveHomePlayer(localId: String) {
        _uiState.update { it.copy(homePlayers = it.homePlayers.filterNot { player -> player.localId == localId }) }
    }

    fun onRemoveAwayPlayer(localId: String) {
        _uiState.update { it.copy(awayPlayers = it.awayPlayers.filterNot { player -> player.localId == localId }) }
    }

    fun onSave() {
        viewModelScope.launch {
            val form = _uiState.value
            if (form.homePlayers.isEmpty() || form.awayPlayers.isEmpty()) {
                _uiState.update { it.copy(errorMessage = DomainError.InvalidRoster.toUiMessage()) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val settings = MatchSettings(
                setsToWin = form.setsToWin,
                pointsToWinSet = form.pointsToWinSet,
                pointsToWinLastSet = form.pointsToWinLastSet,
                mustWinByTwo = form.mustWinByTwo,
            )
            val existingId = matchId
            val result = if (existingId == null) {
                createMatch(form.homeName, form.awayName, settings)
            } else {
                updateMatchSetup(existingId, form.homeName, form.awayName, settings)
            }
            when (result) {
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.error.toUiMessage())
                    }
                }
                is DomainResult.Success -> {
                    val match = result.data
                    val playersResult = persistPlayers(
                        matchId = match.id,
                        homeTeamId = match.home.id,
                        awayTeamId = match.away.id,
                        homePlayers = form.homePlayers,
                        awayPlayers = form.awayPlayers,
                    )
                    when (playersResult) {
                        is DomainResult.Failure -> {
                            _uiState.update {
                                it.copy(isLoading = false, errorMessage = playersResult.error.toUiMessage())
                            }
                        }
                        is DomainResult.Success -> {
                            _uiState.update { it.copy(isLoading = false) }
                            _events.send(MatchSetupEvent.Saved(match.id))
                        }
                    }
                }
            }
        }
    }

    private fun addDraftPlayer(homeTeam: Boolean) {
        val form = _uiState.value
        val name = if (homeTeam) form.homePlayerDraft else form.awayPlayerDraft
        if (name.isBlank()) {
            _uiState.update { it.copy(errorMessage = DomainError.BlankPlayerName.toUiMessage()) }
            return
        }
        val player = SetupPlayerDraft(localId = "draft-${nextPlayerId++}", name = name.trim())
        _uiState.update {
            if (homeTeam) {
                it.copy(homePlayers = it.homePlayers + player, homePlayerDraft = "", errorMessage = null)
            } else {
                it.copy(awayPlayers = it.awayPlayers + player, awayPlayerDraft = "", errorMessage = null)
            }
        }
    }

    private suspend fun persistPlayers(
        matchId: String,
        homeTeamId: String,
        awayTeamId: String,
        homePlayers: List<SetupPlayerDraft>,
        awayPlayers: List<SetupPlayerDraft>,
    ): DomainResult<Unit> {
        homePlayers.filter { it.localId.startsWith("draft-") }.forEach { player ->
            val result = addPlayer(matchId, homeTeamId, player.name)
            if (result is DomainResult.Failure) return result
        }
        awayPlayers.filter { it.localId.startsWith("draft-") }.forEach { player ->
            val result = addPlayer(matchId, awayTeamId, player.name)
            if (result is DomainResult.Failure) return result
        }
        return DomainResult.Success(Unit)
    }

    companion object {
        const val MATCH_ID_KEY = "matchId"
    }
}
