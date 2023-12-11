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
import fr.harmoniamk.statsmk.compose.screen.PlayerProfileScreen
import fr.harmoniamk.statsmk.compose.screen.PlayersSettingsScreen
import fr.harmoniamk.statsmk.compose.screen.PositionScreen
import fr.harmoniamk.statsmk.compose.screen.ProfileScreen
import fr.harmoniamk.statsmk.compose.screen.SignupScreen
import fr.harmoniamk.statsmk.compose.screen.StatsRankingScreen
import fr.harmoniamk.statsmk.compose.screen.StatsScreen
import fr.harmoniamk.statsmk.compose.screen.TeamListScreen
import fr.harmoniamk.statsmk.compose.screen.TeamProfileScreen
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
                onSettingsItemClick = { navController.navigate(it) }
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
            TeamSettingsScreen(onPlayerClick = { navController.navigate("Home/Settings/PlayerProfile/$it") })
        }
        composable(
            route = "Home/Settings/PlayerProfile/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val playerId = it.arguments?.getString("id")
            PlayerProfileScreen(id = playerId.orEmpty())
        }
        composable(
            route = "Home/Settings/TeamProfile/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val playerId = it.arguments?.getString("id")
            TeamProfileScreen(id = playerId.orEmpty(), onPlayerClick = { navController.navigate("Home/Settings/PlayerProfile/$it") })
        }
        composable("Home/Settings/Players") {
            PlayersSettingsScreen(onBack = { navController.popBackStack() })
        }
        composable("Home/Settings/Opponents") {
            OpponentSettingsScreen(onTeamClick = { navController.navigate("Home/Settings/TeamProfile/$it") })
        }
        composable("Home/Settings/Profile") {
            ProfileScreen(onLogout = {
                navController.navigate("Login")
            })
        }

        /** Opponent stats navigation **/
        composable("Home/Stats/Opponents") {
            StatsRankingScreen(state = StatsRankingState.OpponentRankingState()) { item, _, _ ->
                navController.navigate("Home/Stats/Opponents/${(item as? OpponentRankingItemViewModel)?.team?.team_id.orEmpty()}/${(item as? OpponentRankingItemViewModel)?.userId.orEmpty()}")
            }
        }
        /** Players stats navigation **/
        composable("Home/Stats/Players") {
            StatsRankingScreen(state = StatsRankingState.PlayerRankingState()) { item, _, _ ->
                navController.navigate("Home/Stats/Players/${(item as? PlayerRankingItemViewModel)?.user?.player_id.orEmpty()}")
            }
        }
        composable(
            route = "Home/Stats/Players/{id}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
            )
        ) {
            StatsScreen(
                type = StatsType.IndivStats(userId = it.arguments?.getString("id").orEmpty()),
                onWarDetailsClick = { type, _ -> navController.navigate("Home/War/AllWars/Player/${(type as? StatsType.IndivStats)?.userId}") },
                onTrackDetailsClick = { userId, teamId ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId")
                    }
                }, goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/")
                        else -> {}
                    }
                },
                goToMapStats = { trackIndex, teamId, userId ->
                    when {
                        teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId"))
                    }
                }
            )
        }

        /** Team stats navigation **/
        composable(route = "Home/Stats/Team") {
            StatsScreen(
                type = StatsType.TeamStats(),
                onWarDetailsClick = { _, _ -> navController.navigate("Home/War/AllWars") },
                goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/")
                        else -> {}
                    }
                },
                onTrackDetailsClick = { userId, teamId ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId")
                    }
                }, goToMapStats = { trackIndex, teamId, userId ->
                    when {
                        teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId"))
                    }
                }
            )
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
                ),
                onWarDetailsClick = { type, _ -> navController.navigate("Home/War/AllWars/Team/${(type as? StatsType.OpponentStats)?.teamId}") },
                goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/")
                        else -> {}
                    }
                },
                onTrackDetailsClick = { userId, teamId ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId")
                    }
                },
                goToMapStats = { trackIndex, teamId, userId ->
                    when {
                        teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId"))
                    }
                }
            )
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
                ),
                onWarDetailsClick = { type, _ -> navController.navigate("Home/War/AllWars/Team/${(type as? StatsType.OpponentStats)?.teamId}/${(type as? StatsType.OpponentStats)?.userId}") },
                goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/")
                        else -> {}
                    }
                },
                onTrackDetailsClick = { userId, teamId ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId")
                    }
                },
                goToMapStats = { trackIndex, teamId, userId ->
                    when {
                        teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId"))
                    }
                })
        }

        /** Maps stats navigation **/
        composable("Home/Stats/Maps/Ranking") {

            StatsRankingScreen(state = StatsRankingState.MapsRankingState()) { item, userId, teamId ->
                when {
                    userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/User/$userId")
                    teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/Team/$teamId")
                }
            }
        }
        composable(
            "Home/Stats/Maps/Ranking/User/{userId}", arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
            )
        ) {
            StatsRankingScreen(
                state = StatsRankingState.MapsRankingState(),
                userId = it.arguments?.getString("userId")
            ) { item, userId, teamId ->
                when {
                    userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/User/$userId")
                    teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/Team/$teamId")
                }
            }
        }
        composable(
            "Home/Stats/Maps/Ranking/Team/{teamId}", arguments = listOf(
                navArgument("teamId") { type = NavType.StringType },
            )
        ) {
            StatsRankingScreen(
                state = StatsRankingState.MapsRankingState(),
                teamId = it.arguments?.getString("teamId")
            ) { item, userId, teamId ->
                when {
                    teamId?.takeIf { it.isNotEmpty() } != null && userId?.takeIf { it.isNotEmpty() } == null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/Team/$teamId")
                    teamId?.takeIf { it.isNotEmpty() } != null && userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/User/$userId/Team/$teamId")
                    userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/User/$userId")
                }
            }
        }
        composable(
            "Home/Stats/Maps/{trackId}/User/{userId}", arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.StringType },
            )
        ) {
            StatsScreen(type = StatsType.MapStats(
                trackIndex = it.arguments?.getInt("trackId") ?: 0,
                userId = it.arguments?.getString("userId").orEmpty()
            ),
                onWarDetailsClick = { type, _ ->
                    (type as? StatsType.MapStats)?.userId?.let {
                        navController.navigate("Home/Stats/Maps/${type.trackIndex}/User/${type.userId}/Details")
                    }
                },
                goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/")
                        else -> {}
                    }
                },
                onTrackDetailsClick = { userId, teamId ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId")
                    }
                }, goToMapStats = { trackIndex, teamId, userId ->
                    when {
                        teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId"))
                    }
                })
        }
        composable(
            "Home/Stats/Maps/{trackId}/User/{userId}/Team/{teamId}", arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("teamId") { type = NavType.StringType }
            )
        ) {
            StatsScreen(type = StatsType.MapStats(
                trackIndex = it.arguments?.getInt("trackId") ?: 0,
                userId = it.arguments?.getString("userId").orEmpty(),
                teamId = it.arguments?.getString("teamId").orEmpty(),
            ),
                onWarDetailsClick = { type, _ ->
                    (type as? StatsType.MapStats)?.userId?.let {
                        navController.navigate("Home/Stats/Maps/${type.trackIndex}/User/${type.userId}/Details")
                    }
                },
                goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/")
                        else -> {}
                    }
                },
                onTrackDetailsClick = { userId, teamId ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId")
                    }
                }, goToMapStats = { trackIndex, teamId, userId ->
                    when {
                        teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId"))
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
                ),
                onWarDetailsClick = { type, _ ->
                    (type as? StatsType.MapStats)?.teamId?.let {
                        navController.navigate("Home/Stats/Maps/${type.trackIndex}/Team/${type.teamId}/Details")
                    }
                },
                goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/")
                        else -> {}
                    }
                },
                onTrackDetailsClick = { userId, teamId ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId")
                    }
                }, goToMapStats = { trackIndex, teamId, userId ->
                    when {
                        teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId"))
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
            StatsScreen(
                type = StatsType.PeriodicStats(),
                onWarDetailsClick = { _, isWeek -> navController.navigate("Home/War/AllWars/Periodic/$isWeek") },
                goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/")
                        else -> {}
                    }
                },
                onTrackDetailsClick = { userId, teamId ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId")
                    }
                }, goToMapStats = { trackIndex, teamId, userId ->
                    when {
                        teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId"))
                    }
                })
        }

        /** War list navigation **/
        composable(
            route = "Home/War/AllWars"
        ) {
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
        }
        /** War list navigation **/

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