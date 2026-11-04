package br.com.matchpoint.presentation.home

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState
    data class Content(val matches: List<HomeMatchItem>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

data class HomeMatchItem(
    val id: String,
    val homeName: String,
    val awayName: String,
    val setsScore: String,
    val statusLabel: String,
)
