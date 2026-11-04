package br.com.matchpoint.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class MatchAggregate(
    @Embedded val match: MatchEntity,
    @Relation(parentColumn = "id", entityColumn = "matchId")
    val teams: List<TeamEntity>,
    @Relation(parentColumn = "id", entityColumn = "matchId")
    val players: List<PlayerEntity>,
    @Relation(parentColumn = "id", entityColumn = "matchId")
    val sets: List<SetEntity>,
    @Relation(parentColumn = "id", entityColumn = "matchId")
    val points: List<PointEntity>,
)
