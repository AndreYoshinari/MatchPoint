package br.com.matchpoint.domain.usecase

import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow

class ObserveMatchHistoryUseCase(
    private val repository: MatchRepository,
) {
    operator fun invoke(): Flow<List<Match>> = repository.observeAll()
}
