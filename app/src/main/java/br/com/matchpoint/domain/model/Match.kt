package br.com.matchpoint.domain.model

data class Match(
    val id: String,
    val status: MatchStatus,
    val settings: MatchSettings,
    val home: Team,
    val away: Team,
    val players: List<Player> = emptyList(),
    val sets: List<VolleyballSet> = emptyList(),
    val points: List<Point> = emptyList(),
    val servingTeamId: String? = null,
    val initialServingTeamId: String? = null,
    val startedAt: Long? = null,
    val finishedAt: Long? = null,
    val winnerTeamId: String? = null,
) {
    val currentSet: VolleyballSet?
        get() = sets.lastOrNull { !it.isFinished } ?: sets.lastOrNull()

    val homeSetsWon: Int
        get() = sets.count { it.winnerTeamId == home.id }

    val awaySetsWon: Int
        get() = sets.count { it.winnerTeamId == away.id }

    val winner: Team?
        get() = when (winnerTeamId) {
            home.id -> home
            away.id -> away
            else -> null
        }

    fun playersOf(teamId: String): List<Player> = players.filter { it.teamId == teamId }

    fun playerById(playerId: String): Player? = players.find { it.id == playerId }

    fun isHome(teamId: String): Boolean = teamId == home.id

    fun isAway(teamId: String): Boolean = teamId == away.id

    fun isParticipant(teamId: String): Boolean = isHome(teamId) || isAway(teamId)
}
