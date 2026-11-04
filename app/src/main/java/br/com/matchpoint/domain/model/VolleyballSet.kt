package br.com.matchpoint.domain.model

data class VolleyballSet(
    val index: Int,
    val homePoints: Int = 0,
    val awayPoints: Int = 0,
    val winnerTeamId: String? = null,
) {
    val isFinished: Boolean get() = winnerTeamId != null
}
