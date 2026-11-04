package br.com.matchpoint.domain.repository

import br.com.matchpoint.domain.model.Match
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    suspend fun create(match: Match)
    suspend fun update(match: Match)
    suspend fun getById(matchId: String): Match?
    fun observeById(matchId: String): Flow<Match?>
    fun observeAll(): Flow<List<Match>>
    suspend fun getAll(): List<Match>
}
