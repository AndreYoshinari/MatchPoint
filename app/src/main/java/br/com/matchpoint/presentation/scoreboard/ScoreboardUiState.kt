package br.com.matchpoint.presentation.scoreboard

import br.com.matchpoint.domain.model.Match

sealed interface ScoreboardUiState {
    data object Loading : ScoreboardUiState
    data class Active(val match: Match, val actionError: String? = null) : ScoreboardUiState
    data class Finished(val match: Match, val actionError: String? = null) : ScoreboardUiState
    data class Error(val message: String) : ScoreboardUiState
}
