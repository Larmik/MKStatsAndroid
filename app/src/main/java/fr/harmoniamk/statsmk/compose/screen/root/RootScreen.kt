package fr.harmoniamk.statsmk.compose.screen.root

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.harmoniamk.statsmk.compose.screen.CurrentWarScreen
import fr.harmoniamk.statsmk.compose.screen.LoginScreen
import fr.harmoniamk.statsmk.compose.screen.OpponentSettingsScreen
import fr.harmoniamk.statsmk.compose.screen.PlayerListScreen
import fr.harmoniamk.statsmk.compose.screen.PlayersSettingsScreen
import fr.harmoniamk.statsmk.compose.screen.PositionScreen
import fr.harmoniamk.statsmk.compose.screen.ProfileScreen
import fr.harmoniamk.statsmk.compose.screen.SignupScreen
import fr.harmoniamk.statsmk.compose.screen.StatsRankingScreen
import fr.harmoniamk.statsmk.compose.screen.StatsScreen
import fr.harmoniamk.statsmk.compose.screen.TeamListScreen
import fr.harmoniamk.statsmk.compose.screen.TeamSettingsScreen
import fr.harmoniamk.statsmk.compose.screen.TrackDetailsScreen
import fr.harmoniamk.statsmk.compose.screen.TrackListScreen
import fr.harmoniamk.statsmk.compose.screen.WarDetailsScreen
import fr.harmoniamk.statsmk.compose.screen.WarListScreen
import fr.harmoniamk.statsmk.compose.screen.WarTrackListScreen
import fr.harmoniamk.statsmk.compose.screen.WarTrackResultScreen
import fr.harmoniamk.statsmk.compose.viewModel.StatsRankingState
import fr.harmoniamk.statsmk.compose.viewModel.StatsType
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.local.TrackStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@ExperimentalMaterialApi
@Composable
fun RootScreen(startDestination: String = "Login", onBack: () -> Unit) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable(route = "Login") {
            LoginScreen(
                onNext = { navController.navigate("Home") },
                onSignup = { navController.navigate("Signup") },
                onBack = onBack
            )
        }
        composable(route = "Signup") {
            SignupScreen(
                onLogin = { navController.navigate("Login") },
                onBack = onBack,
                onNext = { navController.navigate("Home") }
            )
        }
        composable(route = "Home") {
            HomeScreen(
                onBack = onBack,
                onCurrentWarClick = { navController.navigate("Home/War/Current") },
                onWarClick = { navController.navigate(route = "Home/War/$it") },
                onCreateWarClick = { navController.navigate("Home/War/AddWar") },
                onSettingsItemClick = { navController.navigate(it) },
                onAllWarsClick = { navController.navigate("Home/War/AllWars") }
            )
        }


        /** Navigation of add war **/
        composable(route = "Home/War/AddWar") {
            TeamListScreen(
                onTeamClick = { navController.navigate("Home/War/AddWar/$it/AddPlayers") }
            )
        }
        composable(
            route = "Home/War/AddWar/{team}/AddPlayers",
            arguments = listOf(navArgument("team") { type = NavType.StringType })
        ) {
            PlayerListScreen(
                it.arguments?.getString("team"),
                onWarStarted = { navController.navigate(route = "Home/War/Current") })
        }

        /** Navigation of Current War **/
        composable(route = "Home/War/Current") {
            CurrentWarScreen(
                onNextTrack = { navController.navigate("Home/War/Current/AddTrack") },
                onBack = { navController.popBackStack("Home", inclusive = false) },
                onTrackClick = { navController.navigate("Home/War/Current/TrackDetails/$it") },
                onRedirectToResume = {
                    navController.navigate(route = "Home/War/$it") {
                        popUpTo("Home/War/Current") {
                            inclusive = true
                        }
                    }
                }
            )
        }
        composable("Home/War/Current/AddTrack") {
            TrackListScreen(onTrackClick = { index -> navController.navigate("Home/War/Current/AddTrack/$index/AddPosition") })
        }
        composable(
            "Home/War/Current/AddTrack/{index}/AddPosition",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) {
            val index = it.arguments?.getInt("index") ?: -1
            PositionScreen(
                trackIndex = index,
                editing = false,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("Home/War/Current/AddTrack/$index/AddPosition/Result") }
            )
        }
        composable(
            route = "Home/War/Current/AddTrack/{index}/AddPosition/Result",
            arguments = listOf(navArgument("index") { type = NavType.IntType })
        ) {
            val index = it.arguments?.getInt("index") ?: -1
            WarTrackResultScreen(
                trackIndex = index,
                editing = false,
                onBack = {
                    navController.popBackStack(
                        route = "Home/War/Current/AddTrack/$index/AddPosition",
                        inclusive = true
                    )
                },
                backToCurrent = {
                    navController.popBackStack(
                        route = "Home/War/Current/AddTrack",
                        inclusive = true
                    )
                },
            )
        }

        /** War details navigation**/
        composable(
            route = "Home/War/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val warId = it.arguments?.getString("id")
            WarDetailsScreen(
                id = warId,
                onTrackClick = { navController.navigate("Home/War/$warId/TrackDetails/$it") })
        }
        composable(
            route = "Home/War/{id}/TrackDetails/{track_id}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("track_id") { type = NavType.StringType }
            )) {
            TrackDetailsScreen(
                warId = it.arguments?.getString("id") ?: "",
                warTrackId = it.arguments?.getString("track_id") ?: "",
                onBack = { navController.popBackStack() })
        }

        /** Settings navigation **/
        composable("Home/Settings/Team") {
            TeamSettingsScreen()
        }
        composable("Home/Settings/Players") {
            PlayersSettingsScreen(onBack = { navController.popBackStack() }, canAdd = false)
        }
        composable("Home/Settings/Opponents") {
            OpponentSettingsScreen()
        }
        composable("Home/Settings/Profile") {
            ProfileScreen(onLogout = {
                navController.navigate("Login")
            })
        }

        /** Opponent stats navigation **/
        composable("Home/Stats/Opponents") {
            StatsRankingScreen(state = StatsRankingState.OpponentRankingState()) { item, _, _ ->
                navController.navigate("Home/Stats/Opponents/${(item as? OpponentRankingItemViewModel)?.team?.mid.orEmpty()}/${(item as? OpponentRankingItemViewModel)?.userId.orEmpty()}")
            }
        }
        /** Players stats navigation **/
        composable("Home/Stats/Players") {
            StatsRankingScreen(state = StatsRankingState.PlayerRankingState()) { item, _, _ ->
                navController.navigate("Home/Stats/Players/${(item as? PlayerRankingItemViewModel)?.user?.mid.orEmpty()}")
            }
        }
        composable(
            route = "Home/Stats/Players/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
            )
        ) {
            StatsScreen(type = StatsType.IndivStats(
                userId = it.arguments?.getString("id").orEmpty()
            ),
                onDetailsClick = {type, _ -> navController.navigate("Home/War/AllWars/Player/${(type as? StatsType.IndivStats)?.userId}") })
        }

        /** Team stats navigation **/
        composable(route = "Home/Stats/Team") {
            StatsScreen(type = StatsType.TeamStats(), onDetailsClick = { _, _ ->
                navController.navigate("Home/War/AllWars")
            })
        }

        composable(
            route = "Home/Stats/Opponents/{teamId}/",
            arguments = listOf(
                navArgument("teamId") { type = NavType.StringType }
            )
        ) {
            StatsScreen(
                type = StatsType.OpponentStats(
                    teamId = it.arguments?.getString("teamId").orEmpty()
                ), onDetailsClick = {type, _ ->
                    navController.navigate("Home/War/AllWars/Team/${(type as? StatsType.OpponentStats)?.teamId}")
                })
        }
        composable(
            route = "Home/Stats/Opponents/{teamId}/{userId}",
            arguments = listOf(
                navArgument("teamId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
            )
        ) {
            StatsScreen(
                type = StatsType.OpponentStats(
                    teamId = it.arguments?.getString("teamId").orEmpty(),
                    userId = it.arguments?.getString("userId").orEmpty()
                ), onDetailsClick = { type, _ ->
                    navController.navigate("Home/War/AllWars/Team/${(type as? StatsType.OpponentStats)?.teamId}/${(type as? StatsType.OpponentStats)?.userId}")
                })
        }

        /** Maps stats navigation **/
        composable("Home/Stats/Maps") {
            StatsRankingScreen(state = StatsRankingState.MapsRankingState()) { item, userId, teamId ->
                when {
                    userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/User/$userId")
                    teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/Team/$teamId")
                }
            }
        }
        composable(
            "Home/Stats/Maps/{trackId}/User/{userId}", arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.StringType },
            )
        ) {
            StatsScreen(
                type = StatsType.MapStats(
                    trackIndex = it.arguments?.getInt("trackId") ?: 0,
                    userId = it.arguments?.getString("userId").orEmpty()
                ), onDetailsClick = {type, _ ->
                    (type as? StatsType.MapStats)?.userId?.let {
                        navController.navigate("Home/Stats/Maps/${type.trackIndex}/User/${type.userId}/Details")
                    }
                })
        }
        composable(
            "Home/Stats/Maps/{trackId}/Team/{teamId}", arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("teamId") { type = NavType.StringType },
            )
        ) {
            StatsScreen(
                type = StatsType.MapStats(
                    trackIndex = it.arguments?.getInt("trackId") ?: 0,
                    teamId = it.arguments?.getString("teamId").orEmpty()
                ), onDetailsClick = { type, _ ->
                    (type as? StatsType.MapStats)?.teamId?.let {
                        navController.navigate("Home/Stats/Maps/${type.trackIndex}/Team/${type.teamId}/Details")
                    }


                })
        }
        composable(
            route = "Home/Stats/Maps/{trackId}/User/{userId}/Details",
            arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.StringType },
            )
        ) {
            WarTrackListScreen(
                trackIndex = it.arguments?.getInt("trackId") ?: 0,
                userId = it.arguments?.getString("userId")
            )

        }
        composable(
            route = "Home/Stats/Maps/{trackId}/Team/{teamId}/Details",
            arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("teamId") { type = NavType.StringType },
            )
        ) {
            WarTrackListScreen(
                trackIndex = it.arguments?.getInt("trackId") ?: 0,
                teamId = it.arguments?.getString("teamId")
            )

        }

        /** Periodic stats navigation **/
        composable("Home/Stats/Periodic") {
            StatsScreen(type = StatsType.PeriodicStats(), onDetailsClick = {_, isWeek ->
                navController.navigate("Home/War/AllWars/Periodic/$isWeek")
            })
        }

        /** War list navigation **/
        composable(
            route = "Home/War/AllWars") {
            WarListScreen() { navController.navigate("Home/War/$it") }
        }
        composable(
            route = "Home/War/AllWars/Team/{teamId}",
            arguments = listOf(
                navArgument("teamId") { type = NavType.StringType },
            )
        ) {
            WarListScreen(
                teamId = it.arguments?.getString("teamId")
            ) { navController.navigate("Home/War/$it") }
        }  /** War list navigation **/

        composable(
            route = "Home/War/AllWars/Team/{teamId}/{userId}",
            arguments = listOf(
                navArgument("teamId") { type = NavType.StringType },
                navArgument("userId") { type = NavType.StringType },
            )
        ) {
            WarListScreen(
                teamId = it.arguments?.getString("teamId"),
                userId = it.arguments?.getString("userId")
            )
            { navController.navigate("Home/War/$it") }
        }
        composable(
            route = "Home/War/AllWars/Player/{userId}",
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
            )
        ) {
            WarListScreen(
                userId = it.arguments?.getString("userId")
            ) { navController.navigate("Home/War/$it") }
        }
        composable(
            route = "Home/War/AllWars/Periodic/{isWeek}",
            arguments = listOf(
                navArgument("isWeek") { type = NavType.BoolType },
            )
        ) {
            WarListScreen(
                isWeek = it.arguments?.getBoolean("isWeek")
            ) { navController.navigate("Home/War/$it") }
        }
    }
}