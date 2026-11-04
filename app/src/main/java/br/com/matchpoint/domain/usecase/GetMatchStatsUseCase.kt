package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.MatchStats
import br.com.matchpoint.domain.model.PlayerStat
import br.com.matchpoint.domain.repository.MatchRepository

class GetMatchStatsUseCase(
    private val repository: MatchRepository,
) {
    suspend operator fun invoke(matchId: String): DomainResult<MatchStats> {
        val match = repository.getById(matchId)
            ?: return DomainResult.Failure(DomainError.MatchNotFound)
        val pointsByPlayer = match.points.groupingBy { it.playerId }.eachCount()
        val stats = match.players
            .map { player ->
                PlayerStat(
                    playerId = player.id,
                    playerName = player.name,
                    teamId = player.teamId,
                    points = pointsByPlayer[player.id] ?: 0,
                )
            }
            .sortedWith(compareByDescending<PlayerStat> { it.points }.thenBy { it.playerName })
        return DomainResult.Success(MatchStats(matchId = match.id, playerStats = stats))
    }
}
