package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.Clock
import br.com.matchpoint.domain.IdGenerator
import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.repository.MatchRepository
import br.com.matchpoint.domain.scoring.ScoringRules

class RegisterPointUseCase(
    private val repository: MatchRepository,
    private val idGenerator: IdGenerator = IdGenerator.uuid,
    private val clock: Clock = Clock.system,
) {
    suspend operator fun invoke(matchId: String, playerId: String): DomainResult<Match> {
        val match = repository.getById(matchId)
            ?: return DomainResult.Failure(DomainError.MatchNotFound)
        val player = match.playerById(playerId)
            ?: return DomainResult.Failure(DomainError.PlayerNotFound)
        val result = ScoringRules.registerPoint(
            match = match,
            scoringTeamId = player.teamId,
            playerId = playerId,
            pointId = idGenerator.next(),
            atMillis = clock.nowMillis(),
        )
        val updated = result.getOrNull() ?: return result
        repository.update(updated)
        return DomainResult.Success(updated)
    }
}
