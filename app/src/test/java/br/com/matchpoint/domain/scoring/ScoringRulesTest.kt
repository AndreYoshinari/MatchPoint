package br.com.matchpoint.domain.scoring

import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.Match
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.model.Player
import br.com.matchpoint.domain.model.Team
import br.com.matchpoint.domain.model.VolleyballSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScoringRulesTest {

    private val home = Team("home", "Casa")
    private val away = Team("away", "Visitante")
    private val homePlayer = Player("p1", home.id, "Ana")
    private val awayPlayer = Player("p2", away.id, "Bia")
    private var nextPointId = 0

    @Test
    fun pointIncrementsScoreAndKeepsServeWhenServerScores() {
        val updated = score(inProgressMatch(servingTeamId = home.id), homePlayer)
        assertEquals(1, updated.currentSet?.homePoints)
        assertEquals(0, updated.currentSet?.awayPoints)
        assertEquals(home.id, updated.servingTeamId)
    }

    @Test
    fun pointSwitchesServeWhenReceiverScores() {
        val updated = score(inProgressMatch(servingTeamId = home.id), awayPlayer)
        assertEquals(away.id, updated.servingTeamId)
        assertEquals(1, updated.currentSet?.awayPoints)
    }

    @Test
    fun setContinuesAtTwentyFiveTwentyFourWhenWinByTwo() {
        var match = inProgressMatch()
        repeat(24) {
            match = score(match, homePlayer)
            match = score(match, awayPlayer)
        }
        match = score(match, homePlayer)
        assertEquals(25, match.currentSet?.homePoints)
        assertEquals(24, match.currentSet?.awayPoints)
        assertNull(match.currentSet?.winnerTeamId)
        assertEquals(MatchStatus.IN_PROGRESS, match.status)
    }

    @Test
    fun setEndsAtTwentySixTwentyFourWithWinByTwo() {
        var match = inProgressMatch()
        repeat(24) {
            match = score(match, homePlayer)
            match = score(match, awayPlayer)
        }
        match = score(match, homePlayer)
        match = score(match, homePlayer)
        assertEquals(1, match.homeSetsWon)
        assertEquals(home.id, match.sets.first().winnerTeamId)
        assertEquals(2, match.sets.size)
        assertEquals(0, match.currentSet?.homePoints)
    }

    @Test
    fun setEndsAtTwentyFiveWhenWinByTwoIsDisabled() {
        var match = inProgressMatch(
            settings = MatchSettings(2, 25, 15, mustWinByTwo = false),
        )
        repeat(24) { match = score(match, homePlayer) }
        match = score(match, match.playerById(homePlayer.id)!!)
        assertEquals(home.id, match.sets.first().winnerTeamId)
    }

    @Test
    fun decidingSetUsesLastSetPoints() {
        var match = inProgressMatch()
        match = winSet(match, homePlayer, 25)
        match = winSet(match, awayPlayer, 25)
        match = winSet(match, homePlayer, 25)
        match = winSet(match, awayPlayer, 25)
        repeat(14) { match = score(match, homePlayer) }
        repeat(13) { match = score(match, awayPlayer) }
        match = score(match, homePlayer)
        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(home.id, match.winnerTeamId)
        assertEquals(3, match.homeSetsWon)
        assertEquals(15, match.sets.last().homePoints)
        assertEquals(13, match.sets.last().awayPoints)
    }

    @Test
    fun matchEndsWhenTeamWinsRequiredSets() {
        var match = inProgressMatch(
            settings = MatchSettings(setsToWin = 2, pointsToWinSet = 25, pointsToWinLastSet = 15, mustWinByTwo = true),
        )
        match = winSet(match, homePlayer, 25)
        match = winSet(match, homePlayer, 25)
        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(home.id, match.winnerTeamId)
        assertEquals(2, match.homeSetsWon)
    }

    @Test
    fun undoLastPointRestoresScoreAndCanReopenFinishedMatch() {
        var match = inProgressMatch(settings = MatchSettings(2, 25, 15, true))
        match = winSet(match, homePlayer, 25)
        match = winSet(match, homePlayer, 25)
        assertEquals(MatchStatus.FINISHED, match.status)

        val undone = success(ScoringRules.undoLastPoint(match))
        assertEquals(MatchStatus.IN_PROGRESS, undone.status)
        assertNull(undone.winnerTeamId)
        assertEquals(24, undone.currentSet?.homePoints)
    }

    @Test
    fun cannotScoreBeforeMatchStarts() {
        val match = inProgressMatch().copy(status = MatchStatus.SETUP, sets = emptyList())
        val result = ScoringRules.registerPoint(match, home.id, homePlayer.id, "pt", 1L)
        assertEquals(DomainError.MatchNotInProgress, (result as DomainResult.Failure).error)
    }

    @Test
    fun cannotUndoWithoutPoints() {
        val result = ScoringRules.undoLastPoint(inProgressMatch())
        assertEquals(DomainError.NoPointsToUndo, (result as DomainResult.Failure).error)
    }

    private fun winSet(match: Match, scorer: Player, target: Int): Match {
        var current = match
        repeat(target) { current = score(current, scorer) }
        return current
    }

    private fun score(match: Match, player: Player): Match {
        nextPointId += 1
        return success(
            ScoringRules.registerPoint(
                match = match,
                scoringTeamId = player.teamId,
                playerId = player.id,
                pointId = "pt$nextPointId",
                atMillis = nextPointId.toLong(),
            ),
        )
    }

    private fun success(result: DomainResult<Match>): Match {
        assertTrue(result is DomainResult.Success)
        return (result as DomainResult.Success).data
    }

    private fun inProgressMatch(
        servingTeamId: String = home.id,
        settings: MatchSettings = MatchSettings.indoorDefault(),
    ): Match {
        nextPointId = 0
        return Match(
            id = "m1",
            status = MatchStatus.IN_PROGRESS,
            settings = settings,
            home = home,
            away = away,
            players = listOf(homePlayer, awayPlayer),
            sets = listOf(VolleyballSet(index = 1)),
            servingTeamId = servingTeamId,
            initialServingTeamId = servingTeamId,
            startedAt = 1L,
        )
    }
}
