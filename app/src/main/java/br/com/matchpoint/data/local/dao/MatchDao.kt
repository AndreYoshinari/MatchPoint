package br.com.matchpoint.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import br.com.matchpoint.data.local.entity.MatchAggregate
import br.com.matchpoint.data.local.entity.MatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MatchEntity)

    @Transaction
    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getAggregate(matchId: String): MatchAggregate?

    @Transaction
    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun observeAggregate(matchId: String): Flow<MatchAggregate?>

    @Transaction
    @Query("SELECT * FROM matches ORDER BY COALESCE(finishedAt, startedAt, 0) DESC, id DESC")
    fun observeAllAggregates(): Flow<List<MatchAggregate>>

    @Transaction
    @Query("SELECT * FROM matches ORDER BY COALESCE(finishedAt, startedAt, 0) DESC, id DESC")
    suspend fun getAllAggregates(): List<MatchAggregate>
}
