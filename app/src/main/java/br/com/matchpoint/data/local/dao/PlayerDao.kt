package br.com.matchpoint.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.matchpoint.data.local.entity.PlayerEntity

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(players: List<PlayerEntity>)

    @Query("DELETE FROM players WHERE matchId = :matchId")
    suspend fun deleteByMatch(matchId: String)
}
