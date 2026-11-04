package br.com.matchpoint.domain.scoring

import br.com.matchpoint.domain.addPoint
import br.com.matchpoint.domain.awayPlayer
import br.com.matchpoint.domain.awayTeam
import br.com.matchpoint.domain.homePlayer
import br.com.matchpoint.domain.homeTeam
import br.com.matchpoint.domain.inProgressMatch
import br.com.matchpoint.domain.model.DomainError
import br.com.matchpoint.domain.model.DomainResult
import br.com.matchpoint.domain.model.MatchSettings
import br.com.matchpoint.domain.model.MatchStatus
import br.com.matchpoint.domain.setupMatch
import br.com.matchpoint.domain.tryAddPoint
import br.com.matchpoint.domain.winSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VolleyballMatchRulesTest {

    @Test
    fun addingAPointIncreasesTheScoringTeamScoreByOne() {
        val match = addPoint(inProgressMatch(), homePlayer)

        assertEquals(1, match.currentSet?.homePoints)
        assertEquals(0, match.currentSet?.awayPoints)
        assertEquals(1, match.points.size)
        assertEquals(homePlayer.id, match.points.single().playerId)
    }

    @Test
    fun addingAPointDoesNotCloseTheSetBeforeTheTarget() {
        val match = addPoint(inProgressMatch(), awayPlayer)

        assertEquals(MatchStatus.IN_PROGRESS, match.status)
        assertNull(match.currentSet?.winnerTeamId)
        assertEquals(1, match.sets.size)
    }

    @Test
    fun winningASetStartsTheNextSetAtZeroWhileKeepingThePreviousResult() {
        val afterFirstSet = winSet(inProgressMatch(), homePlayer, 25)

        assertEquals(2, afterFirstSet.sets.size)
        assertEquals(homeTeam.id, afterFirstSet.sets.first().winnerTeamId)
        assertEquals(25, afterFirstSet.sets.first().homePoints)
        assertEquals(0, afterFirstSet.sets.first().awayPoints)
        assertEquals(0, afterFirstSet.currentSet?.homePoints)
        assertEquals(0, afterFirstSet.currentSet?.awayPoints)
        assertNull(afterFirstSet.currentSet?.winnerTeamId)
        assertEquals(MatchStatus.IN_PROGRESS, afterFirstSet.status)
        assertEquals(1, afterFirstSet.homeSetsWon)
        assertEquals(0, afterFirstSet.awaySetsWon)
    }

    @Test
    fun aSetDoesNotChangeWhileWinByTwoIsStillRequired() {
        var match = inProgressMatch()
        repeat(24) {
            match = addPoint(match, homePlayer)
            match = addPoint(match, awayPlayer)
        }
        match = addPoint(match, homePlayer)

        assertEquals(1, match.sets.size)
        assertEquals(25, match.currentSet?.homePoints)
        assertEquals(24, match.currentSet?.awayPoints)
        assertNull(match.winnerTeamId)
    }

    @Test
    fun theTeamThatReachesTheSetTargetWithATwoPointLeadWinsTheSet() {
        var match = inProgressMatch()
        repeat(24) {
            match = addPoint(match, homePlayer)
            match = addPoint(match, awayPlayer)
        }
        match = addPoint(match, homePlayer)
        match = addPoint(match, homePlayer)

        assertEquals(homeTeam.id, match.sets.first().winnerTeamId)
        assertEquals(26, match.sets.first().homePoints)
        assertEquals(24, match.sets.first().awayPoints)
    }

    @Test
    fun theMatchWinnerIsTheTeamThatWinsTheRequiredNumberOfSets() {
        var match = inProgressMatch(
            settings = MatchSettings(setsToWin = 2, pointsToWinSet = 25, pointsToWinLastSet = 15, mustWinByTwo = true),
        )
        match = winSet(match, awayPlayer, 25)
        match = winSet(match, awayPlayer, 25)

        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(awayTeam.id, match.winnerTeamId)
        assertEquals(awayTeam, match.winner)
        assertEquals(2, match.awaySetsWon)
        assertEquals(0, match.homeSetsWon)
        assertNotNull(match.finishedAt)
    }

    @Test
    fun theHomeTeamWinsABestOfFiveWhenItTakesThreeSets() {
        var match = inProgressMatch()
        match = winSet(match, homePlayer, 25)
        match = winSet(match, awayPlayer, 25)
        match = winSet(match, homePlayer, 25)
        match = winSet(match, homePlayer, 25)

        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(homeTeam.id, match.winnerTeamId)
        assertEquals(3, match.homeSetsWon)
        assertEquals(1, match.awaySetsWon)
    }

    @Test
    fun theDecidingSetUsesTheConfiguredLastSetTarget() {
        var match = inProgressMatch()
        match = winSet(match, homePlayer, 25)
        match = winSet(match, awayPlayer, 25)
        match = winSet(match, homePlayer, 25)
        match = winSet(match, awayPlayer, 25)
        match = winSet(match, awayPlayer, 15)

        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(awayTeam.id, match.winnerTeamId)
        assertEquals(15, match.sets.last().awayPoints)
        assertEquals(0, match.sets.last().homePoints)
    }

    @Test
    fun aFinishedMatchDoesNotOpenAnotherSet() {
        var match = inProgressMatch(
            settings = MatchSettings(setsToWin = 2, pointsToWinSet = 25, pointsToWinLastSet = 15, mustWinByTwo = true),
        )
        match = winSet(match, homePlayer, 25)
        match = winSet(match, homePlayer, 25)

        assertEquals(2, match.sets.size)
        assertTrue(match.sets.all { it.isFinished })
        assertEquals(MatchStatus.FINISHED, match.status)
    }

    @Test
    fun scoringIsRejectedBeforeTheMatchStarts() {
        val result = tryAddPoint(setupMatch(), homePlayer.id, homeTeam.id)

        assertEquals(DomainError.MatchNotInProgress, errorOf(result))
    }

    @Test
    fun scoringIsRejectedAfterTheMatchIsFinished() {
        var match = inProgressMatch(
            settings = MatchSettings(setsToWin = 2, pointsToWinSet = 25, pointsToWinLastSet = 15, mustWinByTwo = true),
        )
        match = winSet(match, homePlayer, 25)
        match = winSet(match, homePlayer, 25)

        val result = tryAddPoint(match, homePlayer.id, homeTeam.id)

        assertEquals(DomainError.MatchAlreadyFinished, errorOf(result))
        assertEquals(MatchStatus.FINISHED, match.status)
        assertEquals(homeTeam.id, match.winnerTeamId)
    }

    @Test
    fun scoringIsRejectedForAnUnknownPlayer() {
        val result = tryAddPoint(inProgressMatch(), "unknown", homeTeam.id)

        assertEquals(DomainError.PlayerNotFound, errorOf(result))
    }

    @Test
    fun scoringIsRejectedWhenThePlayerDoesNotBelongToTheScoringTeam() {
        val result = tryAddPoint(inProgressMatch(), homePlayer.id, awayTeam.id)

        assertEquals(DomainError.PlayerNotFound, errorOf(result))
    }

    @Test
    fun undoIsRejectedBeforeTheMatchStarts() {
        val result = ScoringRules.undoLastPoint(setupMatch())

        assertEquals(DomainError.MatchNotInProgress, errorOf(result))
    }

    @Test
    fun undoIsRejectedWhenThereIsNoPointToRemove() {
        val result = ScoringRules.undoLastPoint(inProgressMatch())

        assertEquals(DomainError.NoPointsToUndo, errorOf(result))
    }

    private fun errorOf(result: DomainResult<*>): DomainError {
        assertTrue(result is DomainResult.Failure)
        return (result as DomainResult.Failure).error
    }
}
