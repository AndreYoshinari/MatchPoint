package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.model.GlobalStats
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.model.PlayerStat
import br.com.matchpoint.domain.model.TeamWinStat
import br.com.matchpoint.domain.repository.MatchRepository

class GetGlobalStatsUseCase(
    private val repository: MatchRepository,
) {
    suspend operator fun invoke(): GlobalStats {
        val matches = repository.getAll()
        val finished = matches.filter { it.status == MatchStatus.FINISHED }
        val teamWins = finished
            .mapNotNull { match -> match.winner?.name }
            .groupingBy { it }
            .eachCount()
            .map { (name, wins) -> TeamWinStat(teamName = name, wins = wins) }
            .sortedWith(compareByDescending<TeamWinStat> { it.wins }.thenBy { it.teamName })
        val totalPoints = finished.sumOf { match -> match.points.size }
        val average = if (finished.isEmpty()) 0.0 else totalPoints.toDouble() / finished.size
        return GlobalStats(
            totalMatches = matches.size,
            finishedMatches = finished.size,
            teamWins = teamWins,
            averagePointsPerMatch = average,
            topScorers = topScorers(matches),
        )
    }

    private fun topScorers(matches: List<Match>): List<PlayerStat> {
        data class Key(val name: String, val teamId: String)

        val aggregated = mutableMapOf<Key, Pair<String, Int>>()
        matches.forEach { match ->
            match.points.forEach { point ->
                val player = match.playerById(point.playerId) ?: return@forEach
                val key = Key(player.name, player.teamId)
                val current = aggregated[key]
                aggregated[key] = (current?.first ?: player.id) to ((current?.second ?: 0) + 1)
            }
        }
        return aggregated
            .map { (key, value) ->
                PlayerStat(
                    playerId = value.first,
                    playerName = key.name,
                    teamId = key.teamId,
                    points = value.second,
                )
            }
            .sortedWith(compareByDescending<PlayerStat> { it.points }.thenBy { it.playerName })
            .take(5)
    }
}
