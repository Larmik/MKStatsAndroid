package fr.harmoniamk.statsmk.compose.screen.root

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import fr.harmoniamk.statsmk.R
import fr.harmoniamk.statsmk.compose.screen.SettingsScreen
import fr.harmoniamk.statsmk.compose.screen.WarScreen
import fr.harmoniamk.statsmk.compose.ui.MKText
import fr.harmoniamk.statsmk.enums.BottomNavItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    onBack: () -> Unit,
    onCurrentWarClick: () -> Unit,
    onWarClick: (String?) -> Unit,
    onCreateWarClick: () -> Unit,
    onSettingsItemClick: (String) -> Unit
) {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem.War,
        BottomNavItem.Stats,
        BottomNavItem.Registry,
        BottomNavItem.Settings,
    )
    BackHandler { onBack() }
    Scaffold(
        bottomBar = {
            BottomNavigation(
                backgroundColor = colorResource(id = R.color.harmonia_dark),
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                painter = painterResource(id = screen.icon),
                                contentDescription = null,
                                modifier = Modifier.size(25.dp)
                            )
                        },
                        label = {
                            MKText(
                                text = screen.title,
                                textColor = if (selected) R.color.white else R.color.boo,
                                fontSize = 12
                            )
                        },
                        selected = selected,
                        selectedContentColor = colorResource(id = R.color.white),
                        unselectedContentColor = colorResource(id = R.color.boo),
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "Home/War") {
            composable(route = "Home/War") {
                WarScreen(
                    onCurrentWarClick = onCurrentWarClick,
                    onWarClick = onWarClick,
                    onCreateWarClick = onCreateWarClick
                )
            }
            composable(route = "Home/Stats") {
                StatsMenuScreen(onItemClick = onSettingsItemClick)
            }
            composable(route = "Home/Registry") {
                RegistryScreen(onItemClick = onSettingsItemClick)
            }
            composable(route = "Home/Settings") {
                SettingsScreen()
            }
        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen(onBack = {}, onCurrentWarClick = {}, onWarClick = {}, onCreateWarClick = { }, onSettingsItemClick = {})
}