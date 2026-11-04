package br.com.matchpoint.domain.model

data class MatchSettings(
    val setsToWin: Int,
    val pointsToWinSet: Int,
    val pointsToWinLastSet: Int,
    val mustWinByTwo: Boolean,
) {
    val maxSets: Int get() = setsToWin * 2 - 1

    fun isValid(): Boolean {
        return setsToWin in VALID_SETS_TO_WIN &&
            pointsToWinSet >= MIN_POINTS &&
            pointsToWinLastSet >= MIN_POINTS
    }

    fun isDecidingSet(homeSetsWon: Int, awaySetsWon: Int): Boolean {
        return homeSetsWon == setsToWin - 1 && awaySetsWon == setsToWin - 1
    }

    fun targetPoints(homeSetsWon: Int, awaySetsWon: Int): Int {
        return if (isDecidingSet(homeSetsWon, awaySetsWon)) pointsToWinLastSet else pointsToWinSet
    }

    companion object {
        private val VALID_SETS_TO_WIN = setOf(2, 3)
        private const val MIN_POINTS = 1

        fun indoorDefault() = MatchSettings(
            setsToWin = 3,
            pointsToWinSet = 25,
            pointsToWinLastSet = 15,
            mustWinByTwo = true,
        )
    }
}
