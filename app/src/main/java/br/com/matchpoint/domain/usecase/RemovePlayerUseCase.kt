package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.repository.MatchRepository

class RemovePlayerUseCase(
    private val repository: MatchRepository,
) {
    suspend operator fun invoke(matchId: String, playerId: String): DomainResult<Match> {
        val match = repository.getById(matchId)
            ?: return DomainResult.Failure(DomainError.MatchNotFound)
        if (match.status != MatchStatus.SETUP) {
            return DomainResult.Failure(DomainError.MatchNotEditable)
        }
        if (match.playerById(playerId) == null) {
            return DomainResult.Failure(DomainError.PlayerNotFound)
        }
        val updated = match.copy(players = match.players.filterNot { it.id == playerId })
        repository.update(updated)
        return DomainResult.Success(updated)
    }
}
