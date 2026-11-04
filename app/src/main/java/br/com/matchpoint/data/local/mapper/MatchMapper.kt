package br.com.matchpoint.data.local.mapper

import br.com.matchpoint.data.local.entity.MatchAggregate
import br.com.matchpoint.data.local.entity.MatchEntity
import br.com.matchpoint.data.local.entity.PlayerEntity
import br.com.matchpoint.data.local.entity.PointEntity
import br.com.matchpoint.data.local.entity.SetEntity
import br.com.matchpoint.data.local.entity.TeamEntity
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.model.Player
import br.com.matchpoint.domain.model.Point
import br.com.matchpoint.domain.model.Team
import br.com.matchpoint.domain.model.VolleyballSet

fun Match.toEntity(): MatchEntity = MatchEntity(
    id = id,
    status = status.name,
    setsToWin = settings.setsToWin,
    pointsToWinSet = settings.pointsToWinSet,
    pointsToWinLastSet = settings.pointsToWinLastSet,
    mustWinByTwo = settings.mustWinByTwo,
    homeTeamId = home.id,
    awayTeamId = away.id,
    servingTeamId = servingTeamId,
    initialServingTeamId = initialServingTeamId,
    startedAt = startedAt,
    finishedAt = finishedAt,
    winnerTeamId = winnerTeamId,
)

fun Match.toTeamEntities(): List<TeamEntity> = listOf(
    TeamEntity(id = home.id, matchId = id, name = home.name),
    TeamEntity(id = away.id, matchId = id, name = away.name),
)

fun Match.toPlayerEntities(): List<PlayerEntity> = players.map { player ->
    PlayerEntity(
        id = player.id,
        matchId = id,
        teamId = player.teamId,
        name = player.name,
        number = player.number,
    )
}

fun Match.toSetEntities(): List<SetEntity> = sets.map { set ->
    SetEntity(
        matchId = id,
        setIndex = set.index,
        homePoints = set.homePoints,
        awayPoints = set.awayPoints,
        winnerTeamId = set.winnerTeamId,
    )
}

fun Match.toPointEntities(): List<PointEntity> = points.map { point ->
    PointEntity(
        id = point.id,
        matchId = id,
        setIndex = point.setIndex,
        scoringTeamId = point.scoringTeamId,
        playerId = point.playerId,
        createdAt = point.createdAt,
    )
}

fun MatchAggregate.toDomain(): Match {
    val homeTeam = teams.first { it.id == match.homeTeamId }.toDomain()
    val awayTeam = teams.first { it.id == match.awayTeamId }.toDomain()
    return Match(
        id = match.id,
        status = MatchStatus.valueOf(match.status),
        settings = MatchSettings(
            setsToWin = match.setsToWin,
            pointsToWinSet = match.pointsToWinSet,
            pointsToWinLastSet = match.pointsToWinLastSet,
            mustWinByTwo = match.mustWinByTwo,
        ),
        home = homeTeam,
        away = awayTeam,
        players = players.map { it.toDomain() },
        sets = sets.sortedBy { it.setIndex }.map { it.toDomain() },
        points = points.sortedBy { it.createdAt }.map { it.toDomain() },
        servingTeamId = match.servingTeamId,
        initialServingTeamId = match.initialServingTeamId,
        startedAt = match.startedAt,
        finishedAt = match.finishedAt,
        winnerTeamId = match.winnerTeamId,
    )
}

private fun TeamEntity.toDomain() = Team(id = id, name = name)

private fun PlayerEntity.toDomain() = Player(
    id = id,
    teamId = teamId,
    name = name,
    number = number,
)

private fun SetEntity.toDomain() = VolleyballSet(
    index = setIndex,
    homePoints = homePoints,
    awayPoints = awayPoints,
    winnerTeamId = winnerTeamId,
)

private fun PointEntity.toDomain() = Point(
    id = id,
    matchId = matchId,
    setIndex = setIndex,
    scoringTeamId = scoringTeamId,
    playerId = playerId,
    createdAt = createdAt,
)
