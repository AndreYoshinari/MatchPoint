package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.Clock
import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.model.VolleyballSet
import br.com.matchpoint.domain.repository.MatchRepository

class StartMatchUseCase(
    private val repository: MatchRepository,
    private val clock: Clock = Clock.system,
) {
    suspend operator fun invoke(matchId: String, servingTeamId: String): DomainResult<Match> {
        val match = repository.getById(matchId)
            ?: return DomainResult.Failure(DomainError.MatchNotFound)
        if (match.status != MatchStatus.SETUP) {
            return DomainResult.Failure(DomainError.MatchNotEditable)
        }
        if (!match.settings.isValid()) {
            return DomainResult.Failure(DomainError.InvalidSettings)
        }
        if (match.playersOf(match.home.id).isEmpty() || match.playersOf(match.away.id).isEmpty()) {
            return DomainResult.Failure(DomainError.InvalidRoster)
        }
        if (!match.isParticipant(servingTeamId)) {
            return DomainResult.Failure(DomainError.InvalidServingTeam)
        }
        val started = match.copy(
            status = MatchStatus.IN_PROGRESS,
            sets = listOf(VolleyballSet(index = 1)),
            servingTeamId = servingTeamId,
            initialServingTeamId = servingTeamId,
            startedAt = clock.nowMillis(),
            finishedAt = null,
            winnerTeamId = null,
        )
        repository.update(started)
        return DomainResult.Success(started)
    }
}
