package br.com.matchpoint.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.matchpoint.data.local.entity.PointEntity

@Dao
interface PointDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(points: List<PointEntity>)

    @Query("DELETE FROM points WHERE matchId = :matchId")
    suspend fun deleteByMatch(matchId: String)
}
