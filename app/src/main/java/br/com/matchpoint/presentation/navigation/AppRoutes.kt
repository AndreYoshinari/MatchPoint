package br.com.matchpoint.presentation.navigation

object AppRoutes {
    const val HOME = "home"
    const val MATCH_SETUP = "match_setup"
    const val ROSTER = "roster"
    const val SCOREBOARD = "scoreboard/{matchId}"
    const val MATCH_DETAIL = "match_detail"
    const val STATS = "stats"

    fun scoreboard(matchId: String) = "scoreboard/$matchId"
}
