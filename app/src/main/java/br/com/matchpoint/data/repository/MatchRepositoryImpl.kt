package br.com.matchpoint.data.repository

import androidx.room.withTransaction
import br.com.matchpoint.data.local.db.MatchPointDatabase
import br.com.matchpoint.data.local.mapper.toDomain
import br.com.matchpoint.data.local.mapper.toEntity
import br.com.matchpoint.data.local.mapper.toPlayerEntities
import br.com.matchpoint.data.local.mapper.toPointEntities
import br.com.matchpoint.data.local.mapper.toSetEntities
import br.com.matchpoint.data.local.mapper.toTeamEntities
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.repository.MatchRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MatchRepositoryImpl(
    private val database: MatchPointDatabase,
) : MatchRepository {

    private val matchDao = database.matchDao()
    private val teamDao = database.teamDao()
    private val playerDao = database.playerDao()
    private val setDao = database.setDao()
    private val pointDao = database.pointDao()

    override suspend fun create(match: Match) {
        upsert(match)
    }

    override suspend fun update(match: Match) {
        upsert(match)
    }

    override suspend fun getById(matchId: String): Match? = withContext(Dispatchers.IO) {
        matchDao.getAggregate(matchId)?.toDomain()
    }

    override fun observeById(matchId: String): Flow<Match?> {
        return matchDao.observeAggregate(matchId)
            .map { aggregate -> aggregate?.toDomain() }
            .flowOn(Dispatchers.IO)
    }

    override fun observeAll(): Flow<List<Match>> {
        return matchDao.observeAllAggregates()
            .map { aggregates -> aggregates.map { it.toDomain() } }
            .flowOn(Dispatchers.IO)
    }

    override suspend fun getAll(): List<Match> = withContext(Dispatchers.IO) {
        matchDao.getAllAggregates().map { it.toDomain() }
    }

    private suspend fun upsert(match: Match) = withContext(Dispatchers.IO) {
        database.withTransaction {
            matchDao.upsert(match.toEntity())
            teamDao.upsertAll(match.toTeamEntities())
            playerDao.deleteByMatch(match.id)
            playerDao.upsertAll(match.toPlayerEntities())
            setDao.deleteByMatch(match.id)
            setDao.upsertAll(match.toSetEntities())
            pointDao.deleteByMatch(match.id)
            pointDao.upsertAll(match.toPointEntities())
        }
    }
}
