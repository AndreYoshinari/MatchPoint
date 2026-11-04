package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.repository.MatchRepository
import br.com.matchpoint.domain.scoring.ScoringRules

class UndoLastPointUseCase(
    private val repository: MatchRepository,
) {
    suspend operator fun invoke(matchId: String): DomainResult<Match> {
        val match = repository.getById(matchId)
            ?: return DomainResult.Failure(DomainError.MatchNotFound)
        val result = ScoringRules.undoLastPoint(match)
        val updated = result.getOrNull() ?: return result
        repository.update(updated)
        return DomainResult.Success(updated)
    }
}
