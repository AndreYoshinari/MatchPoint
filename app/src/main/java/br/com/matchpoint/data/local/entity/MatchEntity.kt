package br.com.matchpoint.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "matches",
    indices = [Index("status"), Index("finishedAt")],
)
data class MatchEntity(
    @PrimaryKey val id: String,
    val status: String,
    val setsToWin: Int,
    val pointsToWinSet: Int,
    val pointsToWinLastSet: Int,
    val mustWinByTwo: Boolean,
    val homeTeamId: String,
    val awayTeamId: String,
    val servingTeamId: String?,
    val initialServingTeamId: String?,
    val startedAt: Long?,
    val finishedAt: Long?,
    val winnerTeamId: String?,
)
