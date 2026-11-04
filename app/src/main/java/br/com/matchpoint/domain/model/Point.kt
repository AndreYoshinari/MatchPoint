package br.com.matchpoint.domain.model

data class Point(
    val id: String,
    val matchId: String,
    val setIndex: Int,
    val scoringTeamId: String,
    val playerId: String,
    val createdAt: Long,
)
