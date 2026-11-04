package br.com.matchpoint.presentation.matchsetup

data class SetupPlayerDraft(
    val localId: String,
    val name: String,
)

data class MatchSetupUiState(
    val isLoading: Boolean = false,
    val homeName: String = "Time A",
    val awayName: String = "Time B",
    val setsToWin: Int = 3,
    val pointsToWinSet: Int = 25,
    val pointsToWinLastSet: Int = 15,
    val mustWinByTwo: Boolean = true,
    val homePlayers: List<SetupPlayerDraft> = emptyList(),
    val awayPlayers: List<SetupPlayerDraft> = emptyList(),
    val homePlayerDraft: String = "",
    val awayPlayerDraft: String = "",
    val errorMessage: String? = null,
)

sealed interface MatchSetupEvent {
    data class Saved(val matchId: String) : MatchSetupEvent
}
