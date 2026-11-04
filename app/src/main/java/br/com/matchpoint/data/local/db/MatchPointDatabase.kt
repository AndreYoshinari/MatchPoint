package br.com.matchpoint.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import br.com.matchpoint.data.local.dao.MatchDao
import br.com.matchpoint.data.local.dao.PlayerDao
import br.com.matchpoint.data.local.dao.PointDao
import br.com.matchpoint.data.local.dao.SetDao
import br.com.matchpoint.data.local.dao.TeamDao
import br.com.matchpoint.data.local.entity.MatchEntity
import br.com.matchpoint.data.local.entity.PlayerEntity
import br.com.matchpoint.data.local.entity.PointEntity
import br.com.matchpoint.data.local.entity.SetEntity
import br.com.matchpoint.data.local.entity.TeamEntity

@Database(
    entities = [
        MatchEntity::class,
        TeamEntity::class,
        PlayerEntity::class,
        SetEntity::class,
        PointEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class MatchPointDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun setDao(): SetDao
    abstract fun pointDao(): PointDao
}
