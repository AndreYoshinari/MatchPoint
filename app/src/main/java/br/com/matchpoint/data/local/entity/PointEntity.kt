package br.com.matchpoint.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "points",
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("matchId"), Index("playerId")],
)
data class PointEntity(
    @PrimaryKey val id: String,
    val matchId: String,
    val setIndex: Int,
    val scoringTeamId: String,
    val playerId: String,
    val createdAt: Long,
)
