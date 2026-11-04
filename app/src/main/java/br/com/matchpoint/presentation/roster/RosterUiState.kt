package br.com.matchpoint.presentation.roster

import br.com.matchpoint.domain.model.Match

sealed interface RosterUiState {
    data object Loading : RosterUiState
    data class Ready(
        val match: Match,
        val servingTeamId: String,
        val actionError: String? = null,
    ) : RosterUiState
    data class Error(val message: String) : RosterUiState
}

sealed interface RosterEvent {
    data class MatchStarted(val matchId: String) : RosterEvent
}
