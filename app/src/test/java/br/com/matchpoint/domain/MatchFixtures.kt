package br.com.matchpoint.domain

import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.model.Player
import br.com.matchpoint.domain.model.Team
import br.com.matchpoint.domain.model.VolleyballSet
import br.com.matchpoint.domain.scoring.ScoringRules
import org.junit.Assert.assertTrue

internal val homeTeam = Team("home", "Casa")
internal val awayTeam = Team("away", "Visitante")
internal val homePlayer = Player("p1", homeTeam.id, "Ana")
internal val awayPlayer = Player("p2", awayTeam.id, "Bia")

internal fun inProgressMatch(
    settings: MatchSettings = MatchSettings.indoorDefault(),
    servingTeamId: String = homeTeam.id,
): Match {
    return Match(
        id = "m1",
        status = MatchStatus.IN_PROGRESS,
        settings = settings,
        home = homeTeam,
        away = awayTeam,
        players = listOf(homePlayer, awayPlayer),
        sets = listOf(VolleyballSet(index = 1)),
        servingTeamId = servingTeamId,
        initialServingTeamId = servingTeamId,
        startedAt = 1L,
    )
}

internal fun setupMatch(
    settings: MatchSettings = MatchSettings.indoorDefault(),
): Match {
    return inProgressMatch(settings).copy(
        status = MatchStatus.SETUP,
        sets = emptyList(),
        servingTeamId = null,
        initialServingTeamId = null,
        startedAt = null,
    )
}

internal fun addPoint(match: Match, player: Player, pointId: String = "pt-${match.points.size}"): Match {
    return success(
        ScoringRules.registerPoint(
            match = match,
            scoringTeamId = player.teamId,
            playerId = player.id,
            pointId = pointId,
            atMillis = match.points.size.toLong() + 1,
        ),
    )
}

internal fun tryAddPoint(
    match: Match,
    playerId: String,
    scoringTeamId: String,
): DomainResult<Match> {
    return ScoringRules.registerPoint(
        match = match,
        scoringTeamId = scoringTeamId,
        playerId = playerId,
        pointId = "pt-try",
        atMillis = 1L,
    )
}

internal fun winSet(match: Match, scorer: Player, target: Int): Match {
    var current = match
    repeat(target) { current = addPoint(current, scorer, "pt-${current.points.size}") }
    return current
}

internal fun <T> success(result: DomainResult<T>): T {
    assertTrue(result is DomainResult.Success)
    return (result as DomainResult.Success).data
}
