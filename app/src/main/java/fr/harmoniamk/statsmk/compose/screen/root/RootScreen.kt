package fr.harmoniamk.statsmk.compose.screen.root

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import fr.harmoniamk.statsmk.activity.MainActivity
import fr.harmoniamk.statsmk.activity.MainViewModel
import fr.harmoniamk.statsmk.compose.screen.CoffeeScreen
import fr.harmoniamk.statsmk.compose.screen.CurrentWarScreen
import fr.harmoniamk.statsmk.compose.screen.FAQScreen
import fr.harmoniamk.statsmk.compose.screen.LoginScreen
import fr.harmoniamk.statsmk.compose.screen.OpponentSettingsScreen
import fr.harmoniamk.statsmk.compose.screen.PlayerListScreen
import fr.harmoniamk.statsmk.compose.screen.PlayerProfileScreen
import fr.harmoniamk.statsmk.compose.screen.PlayersSettingsScreen
import fr.harmoniamk.statsmk.compose.screen.PositionScreen
import fr.harmoniamk.statsmk.compose.screen.ProfileScreen
import fr.harmoniamk.statsmk.compose.screen.SettingsScreen
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
import fr.harmoniamk.statsmk.compose.ui.MKDialog
import fr.harmoniamk.statsmk.compose.ui.MKDialogState
import fr.harmoniamk.statsmk.compose.viewModel.CoffeePurchaseState
import fr.harmoniamk.statsmk.compose.viewModel.StatsRankingState
import fr.harmoniamk.statsmk.compose.viewModel.StatsType
import fr.harmoniamk.statsmk.extension.getActivity
import fr.harmoniamk.statsmk.fragment.stats.opponentRanking.OpponentRankingItemViewModel
import fr.harmoniamk.statsmk.fragment.stats.playerRanking.PlayerRankingItemViewModel
import fr.harmoniamk.statsmk.model.local.TrackStats
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.filterNotNull

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@ExperimentalMaterialApi
@Composable
fun RootScreen(startDestination: String = "Login", onBack: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val mainViewModel: MainViewModel by lazy { ViewModelProvider(context.getActivity() as MainActivity)[MainViewModel::class.java] }
    val coffeeDialog = remember { mutableStateOf<MKDialogState?>(null) }
    
    LaunchedEffect(Unit) {
        mainViewModel.sharedCoffeeState.filterNotNull().collect {
            coffeeDialog.value =  MKDialogState.Error(it.message) {
                coffeeDialog.value = null
            }
        }
    }
    coffeeDialog.value?.let { MKDialog(it) }
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
                onCurrentWarClick = { navController.navigate("Home/War/Current/$it") },
                onWarClick = { navController.navigate(route = "Home/War/$it") },
                onCreateWarClick = { navController.navigate("Home/War/AddWar/$it") },
                onSettingsItemClick = { navController.navigate(it) }
            )
        }


        /** Navigation of add war **/
        composable(
            route = "Home/War/AddWar/{teamHost}",
            arguments = listOf(navArgument("teamHost") { type = NavType.StringType })
        ) {
            val teamHost = it.arguments?.getString("teamHost")
            TeamListScreen(
                onTeamClick = { navController.navigate("Home/War/AddWar/$teamHost/$it") }
            )
        }
        composable(route = "Home/War/AddWar/{teamHost}/{teamOpponent}",
            arguments = listOf(
                navArgument("teamHost") { type = NavType.StringType },
                navArgument("teamOpponent") { type = NavType.StringType }),
        ) {
            PlayerListScreen(
                teamHostId = it.arguments?.getString("teamHost"),
                teamOpponentId = it.arguments?.getString("teamOpponent"),
                onWarStarted = { navController.navigate(route = "Home/War/Current/$it") })
        }

        /** Navigation of Current War **/
        composable(route = "Home/War/Current/{teamId}",    arguments = listOf(navArgument("teamId") { type = NavType.StringType })) {
            CurrentWarScreen(
                teamId = it.arguments?.getString("teamId").orEmpty(),
                onNextTrack = { navController.navigate("Home/War/Current/AddTrack") },
                onBack = { navController.popBackStack("Home", inclusive = false) },
                onTrackClick = { navController.navigate("Home/War/Current/TrackDetails/$it") },
                onRedirectToResume = { warId ->
                    navController.navigate(route = "Home/War/$warId") {
                        popUpTo("Home/War/Current/${it.arguments?.getString("teamId").orEmpty()}") {
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

        /** Registry navigation **/
        composable("Home/Registry/Team") {
            TeamSettingsScreen(onPlayerClick = { navController.navigate("Home/Registry/PlayerProfile/$it") })
        }
        composable(
            route = "Home/Registry/PlayerProfile/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val playerId = it.arguments?.getString("id")
            PlayerProfileScreen(id = playerId.orEmpty())
        }
        composable(
            route = "Home/Registry/TeamProfile/{id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) {
            val playerId = it.arguments?.getString("id")
            TeamProfileScreen(id = playerId.orEmpty(), onPlayerClick = { navController.navigate("Home/Registry/PlayerProfile/$it") })
        }
        composable("Home/Registry/Players") {
            PlayersSettingsScreen(onBack = { navController.popBackStack() }, onPlayerClick = { navController.navigate("Home/Registry/PlayerProfile/$it") })
        }
        composable("Home/Registry/Opponents") {
            OpponentSettingsScreen(onTeamClick = { navController.navigate("Home/Registry/TeamProfile/$it") })
        }
        composable("Home/Registry/Profile") {
            ProfileScreen(onLogout = {
                navController.navigate("Login")
            })
        }
        composable("Home/Registry/Settings") {
            SettingsScreen(onSettingsItemClick = { navController.navigate(it)} )
        }
        composable("Home/Registry/Settings/Help") {
            FAQScreen()
        }
        composable("Home/Registry/Settings/Credits") {
            CreditsScreen()
        }
        composable("Home/Registry/Settings/Coffee") {
            CoffeeScreen()
        }
        /** Opponent stats navigation **/
        composable("Home/Stats/Opponents") {
            StatsRankingScreen(state = StatsRankingState.OpponentRankingState(), periodic = "All") { item, _, _, _ ->
                navController.navigate("Home/Stats/Opponents/${(item as? OpponentRankingItemViewModel)?.team?.team_id.orEmpty()}/${(item as? OpponentRankingItemViewModel)?.userId.orEmpty()}")
            }
        }
        /** Players stats navigation **/
        composable("Home/Stats/Players") {
            StatsRankingScreen(state = StatsRankingState.PlayerRankingState(), periodic = "All") { item, _, _, _ ->
                navController.navigate("Home/Stats/Players/${(item as? PlayerRankingItemViewModel)?.user?.mkcId.orEmpty()}")
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
                onTrackDetailsClick = { userId, teamId, periodic ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId/Periodic/$periodic")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId/Periodic/$periodic")
                        else -> navController.navigate("Home/Stats/Maps/Ranking/Periodic/$periodic")
                    }
                }, goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/")
                        else -> {}
                    }
                },
                goToMapStats = { trackIndex, teamId, userId, periodic ->
                    when {
                        userId != null && teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Team/$teamId/Periodic/$periodic"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Periodic/$periodic"))
                        else -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId/Periodic/$periodic"))
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
                onTrackDetailsClick = { userId, teamId, periodic ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId/Periodic/$periodic")
                        teamId?.takeIf { it.isNotEmpty() } != null  -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId/Periodic/$periodic")
                        else  -> navController.navigate("Home/Stats/Maps/Ranking/Periodic/$periodic")
                    }
                }, goToMapStats = { trackIndex, teamId, userId, periodic ->
                    when {
                        userId != null && teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Team/$teamId/Periodic/$periodic"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Periodic/$periodic"))
                        else -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId/Periodic/$periodic"))
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
                onTrackDetailsClick = { userId, teamId, periodic ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId/Periodic/$periodic")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId/Periodic/$periodic")
                       else -> navController.navigate("Home/Stats/Maps/Ranking/Periodic/$periodic")
                    }
                },
                goToMapStats = { trackIndex, teamId, userId, periodic ->
                    when {
                        userId != null && teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Team/$teamId/Periodic/$periodic"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Periodic/$periodic"))
                        else -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId/Periodic/$periodic"))
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
                    userId = it.arguments?.getString("userId").orEmpty(),
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
                onTrackDetailsClick = { userId, teamId, periodic ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId/Periodic/$periodic")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId/Periodic/$periodic")
                        else -> navController.navigate("Home/Stats/Maps/Ranking/Periodic/$periodic")
                    }
                },
                goToMapStats = { trackIndex, teamId, userId, periodic ->
                    when {
                        userId != null && teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Team/$teamId/Periodic/$periodic"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Periodic/$periodic"))
                        else -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId/Periodic/$periodic"))
                    }
                })
        }

        /** Maps stats navigation **/
        composable("Home/Stats/Maps/Ranking/Periodic/{periodic}",  arguments = listOf(
            navArgument("periodic") { type = NavType.StringType },
        )) {
            StatsRankingScreen(
                state = StatsRankingState.MapsRankingState(),
                periodic = it.arguments?.getString("periodic") ?: "All"
            ) { item, userId, teamId, periodic ->
                when {
                    userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/User/$userId/Periodic/$periodic")
                    else -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/Team/$teamId/Periodic/$periodic")
                }
            }
        }
        composable(
            "Home/Stats/Maps/Ranking/User/{userId}/Periodic/{periodic}", arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("periodic") { type = NavType.StringType },

                )
        ) {
            StatsRankingScreen(
                state = StatsRankingState.MapsRankingState(),
                userId = it.arguments?.getString("userId"),
                periodic = it.arguments?.getString("periodic") ?: "All"
            ) { item, userId, teamId, periodic ->
                when {
                    userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/User/$userId/Periodic/$periodic")
                    teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/Team/$teamId/Periodic/$periodic")
                }
            }
        }
        composable(
            "Home/Stats/Maps/Ranking/Team/{teamId}/Periodic/{periodic}", arguments = listOf(
                navArgument("teamId") {
                    type = NavType.StringType
                    nullable = true
              },
                navArgument("periodic") { type = NavType.StringType },

                )
        ) {
            StatsRankingScreen(
                state = StatsRankingState.MapsRankingState(),
                teamId = it.arguments?.getString("teamId"),
                periodic = it.arguments?.getString("periodic") ?: "All"
            ) { item, userId, teamId, periodic ->
                when {
                    teamId?.takeIf { it.isNotEmpty() } != null && userId?.takeIf { it.isNotEmpty() } == null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/Team/$teamId/Periodic/$periodic")
                    teamId?.takeIf { it.isNotEmpty() } != null && userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/User/$userId/Team/$teamId/Periodic/$periodic")
                    userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/${(item as? TrackStats)?.trackIndex}/User/$userId/Periodic/$periodic")
                }
            }
        }
        composable(
            "Home/Stats/Maps/{trackId}/User/{userId}/Periodic/{periodic}", arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("periodic") { type = NavType.StringType }
            )
        ) {
            StatsScreen(type = StatsType.MapStats(
                trackIndex = it.arguments?.getInt("trackId") ?: 0,
                userId = it.arguments?.getString("userId").orEmpty(),
                periodic = it.arguments?.getString("periodic").orEmpty()
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
                onTrackDetailsClick = { userId, teamId, periodic ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId/Periodic/$periodic")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId/Periodic/$periodic")
                        else -> navController.navigate("Home/Stats/Maps/Ranking//Periodic/$periodic")
                    }
                }, goToMapStats = { trackIndex, teamId, userId, periodic ->
                    when {
                        userId != null && teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Team/$teamId/Periodic/$periodic"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Periodic/$periodic"))
                        else -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId/Periodic/$periodic"))
                    }
                })
        }
        composable(
            "Home/Stats/Maps/{trackId}/User/{userId}/Team/{teamId}/Periodic/{periodic}", arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("userId") { type = NavType.StringType },
                navArgument("periodic") { type = NavType.StringType },
                navArgument("teamId") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            StatsScreen(type = StatsType.MapStats(
                trackIndex = it.arguments?.getInt("trackId") ?: 0,
                userId = it.arguments?.getString("userId").orEmpty(),
                teamId = it.arguments?.getString("teamId").orEmpty(),
                periodic = it.arguments?.getString("periodic").orEmpty()
            ),
                onWarDetailsClick = { type, _ ->
                    (type as? StatsType.MapStats)?.let {
                        when {
                            !it.userId.isNullOrEmpty() && !it.teamId.isNullOrEmpty() -> navController.navigate("Home/Stats/Maps/${type.trackIndex}/User/${type.userId}/Team/${type.teamId}/Details")
                            !it.userId.isNullOrEmpty()  -> navController.navigate("Home/Stats/Maps/${type.trackIndex}/User/${type.userId}/Details")
                            !it.teamId.isNullOrEmpty() -> navController.navigate("Home/Stats/Maps/${type.trackIndex}/Team/${type.teamId}/Details")
                            else -> navController.navigate("Home/Stats/Maps/${type.trackIndex}/Details")
                        }
                    }
                },
                goToWarDetails = { navController.navigate("Home/War/$it") },
                goToOpponentStats = { teamId, userId ->
                    when {
                        userId != null && teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId/$userId")
                        teamId != null -> navController.navigate("Home/Stats/Opponents/$teamId")
                        else -> {}
                    }
                },
                onTrackDetailsClick = { userId, teamId, periodic ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId/Periodic/$periodic")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId/Periodic/$periodic")
                        else -> navController.navigate("Home/Stats/Maps/Ranking/Periodic/$periodic")
                    }
                }, goToMapStats = { trackIndex, teamId, userId, periodic ->
                    when {
                        userId != null && teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Team/$teamId/Periodic/$periodic"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Periodic/$periodic"))
                        else -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId/Periodic/$periodic"))
                    }
                })
        }
        composable(
            "Home/Stats/Maps/{trackId}/Team/{teamId}/Periodic/{periodic}", arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("periodic") { type = NavType.StringType },
                navArgument("teamId") {
                    type = NavType.StringType
                    nullable = true
              },
            )
        ) {
            StatsScreen(
                type = StatsType.MapStats(
                    trackIndex = it.arguments?.getInt("trackId") ?: 0,
                    teamId = it.arguments?.getString("teamId").orEmpty(),
                    periodic = it.arguments?.getString("periodic").orEmpty()
                ),
                onWarDetailsClick = { type, _ ->
                    (type as? StatsType.MapStats)?.let {
                        when {
                            !it.teamId.isNullOrEmpty() && !it.userId.isNullOrEmpty() -> navController.navigate("Home/Stats/Maps/${it.trackIndex}/User/${it.userId}/Team/${it.teamId}/Details")
                            !it.userId.isNullOrEmpty() -> navController.navigate("Home/Stats/Maps/${it.trackIndex}/User/${it.userId}/Details")
                            !it.teamId.isNullOrEmpty() -> navController.navigate("Home/Stats/Maps/${it.trackIndex}/Team/${it.teamId}/Details")
                            else -> navController.navigate("Home/Stats/Maps/${it.trackIndex}/Details")
                        }
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
                onTrackDetailsClick = { userId, teamId, periodic ->
                    when {
                        userId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/User/$userId/Periodic/$periodic")
                        teamId?.takeIf { it.isNotEmpty() } != null -> navController.navigate("Home/Stats/Maps/Ranking/Team/$teamId/Periodic/$periodic")
                        else -> navController.navigate("Home/Stats/Maps/Ranking/Periodic/$periodic")
                    }
                }, goToMapStats = { trackIndex, teamId, userId, periodic ->
                    when {
                        userId != null && teamId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Team/$teamId/Periodic/$periodic"))
                        userId != null -> navController.navigate(("Home/Stats/Maps/$trackIndex/User/$userId/Periodic/$periodic"))
                        else -> navController.navigate(("Home/Stats/Maps/$trackIndex/Team/$teamId/Periodic/$periodic"))
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
                navArgument("teamId") {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) {
            WarTrackListScreen(
                trackIndex = it.arguments?.getInt("trackId") ?: 0,
                teamId = it.arguments?.getString("teamId")
            )

        }
        composable(
            route = "Home/Stats/Maps/{trackId}/User/{userId}/Team/{teamId}/Details",
            arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
                navArgument("teamId") {
                    type = NavType.StringType
                    nullable = true
                },
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) {
            WarTrackListScreen(
                trackIndex = it.arguments?.getInt("trackId") ?: 0,
                teamId = it.arguments?.getString("teamId"),
                userId = it.arguments?.getString("userId"),
            )
        }

        composable(
            route = "Home/Stats/Maps/{trackId}/Details",
            arguments = listOf(
                navArgument("trackId") { type = NavType.IntType },
            )
        ) {
            WarTrackListScreen(
                trackIndex = it.arguments?.getInt("trackId") ?: 0,
            )

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
                navArgument("teamId") {
                    type = NavType.StringType
                    nullable = true
                },
            )
        ) {
            WarListScreen(
                teamId = it.arguments?.getString("teamId")
            ) { navController.navigate("Home/War/$it") }
        }

        composable(
            route = "Home/War/AllWars/Team/{teamId}/{userId}",
            arguments = listOf(
                navArgument("teamId") {
                    type = NavType.StringType
                    nullable = true
                },
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