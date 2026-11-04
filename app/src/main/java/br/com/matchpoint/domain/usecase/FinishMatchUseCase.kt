package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.Clock
import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.repository.MatchRepository

class FinishMatchUseCase(
    private val repository: MatchRepository,
    private val clock: Clock = Clock.system,
) {
    suspend operator fun invoke(matchId: String): DomainResult<Match> {
        val match = repository.getById(matchId)
            ?: return DomainResult.Failure(DomainError.MatchNotFound)
        if (match.status != MatchStatus.IN_PROGRESS) {
            return if (match.status == MatchStatus.FINISHED) {
                DomainResult.Failure(DomainError.MatchAlreadyFinished)
            } else {
                DomainResult.Failure(DomainError.MatchNotInProgress)
            }
        }
        val winnerTeamId = when {
            match.homeSetsWon > match.awaySetsWon -> match.home.id
            match.awaySetsWon > match.homeSetsWon -> match.away.id
            else -> {
                val current = match.currentSet
                when {
                    current == null -> null
                    current.homePoints > current.awayPoints -> match.home.id
                    current.awayPoints > current.homePoints -> match.away.id
                    else -> null
                }
            }
        }
        val finished = match.copy(
            status = MatchStatus.FINISHED,
            winnerTeamId = winnerTeamId,
            finishedAt = clock.nowMillis(),
        )
        repository.update(finished)
        return DomainResult.Success(finished)
    }
}
