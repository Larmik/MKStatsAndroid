package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                onLogin = { navController.navigate("Login")},
                onBack = onBack,
                onNext = { navController.navigate("Home")}
            )
        }
        composable(route = "Home") {
            HomeScreen(
                onBack = onBack, 
                onCurrentWarClick = { navController.navigate("Home/War/Current") },
                onWarClick = { navController.navigate(route = "Home/War/$it") },
                onCreateWarClick = { navController.navigate("Home/War/AddWar")},
                onSettingsItemClick = { navController.navigate(it) }
            )
        }
        composable(route = "Home/War/Current") {
            CurrentWarScreen(onNextTrack = { navController.navigate("Home/War/Current/AddTrack")}, onBack = { navController.popBackStack("Home", inclusive = false)}, onTrackClick = {  navController.navigate("Home/War/Current/TrackDetails/$it") })
        }
        composable(route = "Home/War/{id}",  arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            val warId = it.arguments?.getString("id")
            WarDetailsScreen(id = warId, onTrackClick = { navController.navigate("Home/War/$warId/TrackDetails/$it") })
        }
        composable("Home/War/Current/AddTrack") {
            TrackListScreen(onTrackClick = { index ->  navController.navigate("Home/War/Current/AddTrack/$index/AddPosition")})
        }
        composable("Home/War/Current/AddTrack/{index}/AddPosition",  arguments = listOf(navArgument("index") { type = NavType.IntType })) {
            val index = it.arguments?.getInt("index") ?: -1
            PositionScreen(
                trackIndex = index,
                editing = false,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate("Home/War/Current/AddTrack/$index/AddPosition/Result") }
            )
        }
        composable(route = "Home/War/Current/AddTrack/{index}/AddPosition/Result", arguments = listOf(navArgument("index") { type = NavType.IntType })) {
            val index = it.arguments?.getInt("index") ?: -1
            WarTrackResultScreen(
                trackIndex = index,
                editing = false,
                onBack = { navController.popBackStack(route = "Home/War/Current/AddTrack/$index/AddPosition", inclusive = true) },
                backToCurrent = { navController.popBackStack(route = "Home/War/Current/AddTrack", inclusive = true) },
                goToResume = { navController.popBackStack(route = "Home/War/$it", inclusive = true)}
            )
        }
        composable(route = "Home/War/AddWar") {
            TeamListScreen(
                onTeamClick = { navController.navigate("Home/War/AddWar/$it/AddPlayers")}
            )
        }
        composable(route = "Home/War/AddWar/{team}/AddPlayers", arguments = listOf(navArgument("team") { type = NavType.StringType })) {
            PlayerListScreen(it.arguments?.getString("team"), onWarStarted = { navController.navigate(route = "Home/War/Current")})
        }
        composable(
            route = "Home/War/{id}/TrackDetails/{track_id}",
            arguments = listOf(
                navArgument("id") { type = NavType.StringType },
                navArgument("track_id") { type = NavType.StringType }
            )) {
                TrackDetailsScreen(warId = it.arguments?.getString("id") ?: "", warTrackId = it.arguments?.getString("track_id") ?: "", onBack = { navController.popBackStack() })
            }

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

    }
}