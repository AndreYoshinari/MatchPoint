package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

class ObserveMatchUseCase(
    private val repository: MatchRepository,
) {
    operator fun invoke(matchId: String): Flow<Match?> = repository.observeById(matchId)
}
