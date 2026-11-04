package br.com.matchpoint.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "sets",
    primaryKeys = ["matchId", "setIndex"],
    foreignKeys = [
        ForeignKey(
            entity = MatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["matchId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("matchId")],
)
data class SetEntity(
    val matchId: String,
    val setIndex: Int,
    val homePoints: Int,
    val awayPoints: Int,
    val winnerTeamId: String?,
)
