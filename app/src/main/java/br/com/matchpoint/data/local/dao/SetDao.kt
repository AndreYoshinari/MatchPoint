package br.com.matchpoint.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.matchpoint.data.local.entity.SetEntity

@Dao
interface SetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(sets: List<SetEntity>)

    @Query("DELETE FROM sets WHERE matchId = :matchId")
    suspend fun deleteByMatch(matchId: String)
}
