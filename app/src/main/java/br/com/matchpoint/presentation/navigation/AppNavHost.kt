package br.com.matchpoint.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.matchpoint.presentation.home.HomeRoute
import br.com.matchpoint.presentation.matchsetup.MatchSetupRoute
import br.com.matchpoint.presentation.scoreboard.ScoreboardRoute

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = AppRoutes.HOME,
        modifier = modifier,
    ) {
        composable(AppRoutes.HOME) {
            HomeRoute(
                onNewMatch = { navController.navigate(AppRoutes.MATCH_SETUP) },
                onMatchClick = { matchId -> navController.navigate(AppRoutes.scoreboard(matchId)) },
            )
        }
        composable(AppRoutes.MATCH_SETUP) {
            MatchSetupRoute(
                onBack = { navController.popBackStack() },
                onSaved = { matchId ->
                    navController.navigate(AppRoutes.scoreboard(matchId)) {
                        popUpTo(AppRoutes.HOME)
                    }
                },
            )
        }
        composable(
            route = AppRoutes.SCOREBOARD,
            arguments = listOf(navArgument("matchId") { type = NavType.StringType }),
        ) {
            ScoreboardRoute(
                onBack = { navController.popBackStack() },
            )
        }
    }
}
