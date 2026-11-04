package br.com.matchpoint.domain.scoring

import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.model.Point
import br.com.matchpoint.domain.model.VolleyballSet

object ScoringRules {

    fun registerPoint(
        match: Match,
        scoringTeamId: String,
        playerId: String,
        pointId: String,
        atMillis: Long,
    ): DomainResult<Match> {
        if (match.status != MatchStatus.IN_PROGRESS) {
            return if (match.status == MatchStatus.FINISHED) {
                DomainResult.Failure(DomainError.MatchAlreadyFinished)
            } else {
                DomainResult.Failure(DomainError.MatchNotInProgress)
            }
        }
        val player = match.playerById(playerId)
            ?: return DomainResult.Failure(DomainError.PlayerNotFound)
        if (player.teamId != scoringTeamId || !match.isParticipant(scoringTeamId)) {
            return DomainResult.Failure(DomainError.PlayerNotFound)
        }
        val currentSet = match.sets.lastOrNull { !it.isFinished }
            ?: return DomainResult.Failure(DomainError.MatchAlreadyFinished)

        val point = Point(
            id = pointId,
            matchId = match.id,
            setIndex = currentSet.index,
            scoringTeamId = scoringTeamId,
            playerId = playerId,
            createdAt = atMillis,
        )
        return DomainResult.Success(replay(match, match.points + point))
    }

    fun undoLastPoint(match: Match): DomainResult<Match> {
        if (match.status == MatchStatus.SETUP) {
            return DomainResult.Failure(DomainError.MatchNotInProgress)
        }
        if (match.points.isEmpty()) {
            return DomainResult.Failure(DomainError.NoPointsToUndo)
        }
        return DomainResult.Success(replay(match, match.points.dropLast(1)))
    }

    fun replay(match: Match, points: List<Point>): Match {
        var servingTeamId = match.initialServingTeamId
        val sets = mutableListOf(
            VolleyballSet(index = 1),
        )
        var status = MatchStatus.IN_PROGRESS
        var winnerTeamId: String? = null
        var finishedAt: Long? = null

        for (point in points) {
            if (status == MatchStatus.FINISHED) break
            val current = sets.last()
            val homePoints = current.homePoints + if (point.scoringTeamId == match.home.id) 1 else 0
            val awayPoints = current.awayPoints + if (point.scoringTeamId == match.away.id) 1 else 0
            servingTeamId = point.scoringTeamId

            val completedHomeSets = sets.dropLast(1).count { it.winnerTeamId == match.home.id }
            val completedAwaySets = sets.dropLast(1).count { it.winnerTeamId == match.away.id }
            val target = match.settings.targetPoints(completedHomeSets, completedAwaySets)
            val setWinnerId = setWinner(
                homePoints = homePoints,
                awayPoints = awayPoints,
                target = target,
                mustWinByTwo = match.settings.mustWinByTwo,
                homeTeamId = match.home.id,
                awayTeamId = match.away.id,
            )

            sets[sets.lastIndex] = current.copy(
                homePoints = homePoints,
                awayPoints = awayPoints,
                winnerTeamId = setWinnerId,
            )

            if (setWinnerId == null) continue

            val homeSetsWon = sets.count { it.winnerTeamId == match.home.id }
            val awaySetsWon = sets.count { it.winnerTeamId == match.away.id }
            when {
                homeSetsWon >= match.settings.setsToWin -> {
                    status = MatchStatus.FINISHED
                    winnerTeamId = match.home.id
                    finishedAt = point.createdAt
                }
                awaySetsWon >= match.settings.setsToWin -> {
                    status = MatchStatus.FINISHED
                    winnerTeamId = match.away.id
                    finishedAt = point.createdAt
                }
                else -> sets += VolleyballSet(index = sets.size + 1)
            }
        }

        return match.copy(
            status = status,
            sets = sets,
            points = points,
            servingTeamId = servingTeamId,
            winnerTeamId = winnerTeamId,
            finishedAt = finishedAt,
        )
    }

    fun setWinner(
        homePoints: Int,
        awayPoints: Int,
        target: Int,
        mustWinByTwo: Boolean,
        homeTeamId: String,
        awayTeamId: String,
    ): String? {
        val homeWins = if (mustWinByTwo) {
            homePoints >= target && homePoints - awayPoints >= 2
        } else {
            homePoints >= target && homePoints > awayPoints
        }
        val awayWins = if (mustWinByTwo) {
            awayPoints >= target && awayPoints - homePoints >= 2
        } else {
            awayPoints >= target && awayPoints > homePoints
        }
        return when {
            homeWins -> homeTeamId
            awayWins -> awayTeamId
            else -> null
        }
    }
}
