package br.com.matchpoint.domain.model

data class PlayerStat(
    val playerId: String,
    val playerName: String,
    val teamId: String,
    val points: Int,
)

data class MatchStats(
    val matchId: String,
    val playerStats: List<PlayerStat>,
)

data class TeamWinStat(
    val teamName: String,
    val wins: Int,
)

data class GlobalStats(
    val totalMatches: Int,
    val finishedMatches: Int,
    val teamWins: List<TeamWinStat>,
    val averagePointsPerMatch: Double,
    val topScorers: List<PlayerStat>,
)
