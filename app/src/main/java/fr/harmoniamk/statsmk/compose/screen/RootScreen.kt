package fr.harmoniamk.statsmk.compose.screen

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

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
                onWarClick = {
                    navController.navigate(route = "Home/War/$it")
                }
            )
        }
        composable(route = "Home/War/Current") {
            CurrentWarScreen(onNextTrack = { navController.navigate("Home/War/Current/AddTrack")})
        }
        composable(route = "Home/War/{id}",  arguments = listOf(navArgument("id") { type = NavType.StringType })) {
            WarDetailsScreen(id = it.arguments?.getString("id"))
        }
        composable("Home/War/Current/AddTrack") {
            TrackListScreen(onTrackClick = { index ->  navController.navigate("Home/War/Current/AddTrack/$index/AddPosition")})
        }
        composable("Home/War/Current/AddTrack/{index}/AddPosition",  arguments = listOf(navArgument("index") { type = NavType.IntType })) {
            val index = it.arguments?.getInt("index") ?: -1
            PositionScreen(trackIndex = index, onBack = {
                navController.popBackStack()
            }, onNext = { navController.navigate("Home/War/Current/AddTrack/$index/AddPosition/Result")})
        }
        composable(route = "Home/War/Current/AddTrack/{index}/AddPosition/Result", arguments = listOf(navArgument("index") { type = NavType.IntType })) {
            val index = it.arguments?.getInt("index") ?: -1
            WarTrackResultScreen(
                onBack = { navController.popBackStack(route = "Home/War/Current/AddTrack/$index/AddPosition", inclusive = true) },
                backToCurrent = { navController.popBackStack(route = "Home/War/Current/AddTrack", inclusive = true) },
                goToResume = { navController.popBackStack(route = "Home/War/$it", inclusive = true)}
            )
        }

    }
}