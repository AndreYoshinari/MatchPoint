package br.com.matchpoint.domain.fakes

import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class InMemoryMatchRepository : MatchRepository {
    private val matches = MutableStateFlow<Map<String, Match>>(emptyMap())

    override suspend fun create(match: Match) {
        matches.value = matches.value + (match.id to match)
    }

    override suspend fun update(match: Match) {
        matches.value = matches.value + (match.id to match)
    }

    override suspend fun getById(matchId: String): Match? = matches.value[matchId]

    override fun observeById(matchId: String): Flow<Match?> =
        matches.map { it[matchId] }

    override fun observeAll(): Flow<List<Match>> =
        matches.map { it.values.toList() }

    override suspend fun getAll(): List<Match> = matches.value.values.toList()
}
