package br.com.matchpoint.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import br.com.matchpoint.data.local.entity.TeamEntity

@Dao
interface TeamDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(teams: List<TeamEntity>)
}
