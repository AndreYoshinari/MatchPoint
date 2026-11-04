package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.IdGenerator
import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.model.Player
import br.com.matchpoint.domain.repository.MatchRepository

class AddPlayerUseCase(
    private val repository: MatchRepository,
    private val idGenerator: IdGenerator = IdGenerator.uuid,
) {
    suspend operator fun invoke(
        matchId: String,
        teamId: String,
        name: String,
        number: Int? = null,
    ): DomainResult<Match> {
        val match = repository.getById(matchId)
            ?: return DomainResult.Failure(DomainError.MatchNotFound)
        if (match.status != MatchStatus.SETUP) {
            return DomainResult.Failure(DomainError.MatchNotEditable)
        }
        if (!match.isParticipant(teamId)) {
            return DomainResult.Failure(DomainError.InvalidRoster)
        }
        if (name.isBlank()) return DomainResult.Failure(DomainError.BlankPlayerName)
        val player = Player(
            id = idGenerator.next(),
            teamId = teamId,
            name = name.trim(),
            number = number,
        )
        val updated = match.copy(players = match.players + player)
        repository.update(updated)
        return DomainResult.Success(updated)
    }
}
