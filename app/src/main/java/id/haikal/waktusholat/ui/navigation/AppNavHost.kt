package id.haikal.waktusholat.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import id.haikal.waktusholat.R
import id.haikal.waktusholat.ui.city.CitySearchScreen
import id.haikal.waktusholat.ui.home.HomeScreen
import id.haikal.waktusholat.ui.monthly.MonthlyScreen
import id.haikal.waktusholat.ui.settings.SettingsScreen
import id.haikal.waktusholat.ui.theme.BottomBarTokens

private const val CITY_SEARCH_ROUTE = "city_search"

enum class TopLevelDestination(
    val route: String,
    @param:StringRes val labelRes: Int,
    val icon: ImageVector,
) {
    Home("home", R.string.nav_home, Icons.Filled.Home),
    Monthly("monthly", R.string.nav_monthly, Icons.Filled.DateRange),
    Settings("settings", R.string.nav_settings, Icons.Filled.Settings),
}

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute == null ||
        TopLevelDestination.entries.any { it.route == currentRoute }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(destination.labelRes)) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BottomBarTokens.iconActive,
                                selectedTextColor = BottomBarTokens.iconActive,
                                indicatorColor = BottomBarTokens.indicator,
                                unselectedIconColor = BottomBarTokens.iconInactive,
                                unselectedTextColor = BottomBarTokens.iconInactive,
                            ),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopLevelDestination.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopLevelDestination.Home.route) {
                HomeScreen(onOpenCitySearch = { navController.navigate(CITY_SEARCH_ROUTE) })
            }
            composable(TopLevelDestination.Monthly.route) {
                MonthlyScreen(onOpenCitySearch = { navController.navigate(CITY_SEARCH_ROUTE) })
            }
            composable(TopLevelDestination.Settings.route) {
                SettingsScreen()
            }
            composable(CITY_SEARCH_ROUTE) {
                CitySearchScreen(
                    onBack = { navController.popBackStack() },
                    onLocationSaved = { navController.popBackStack() },
                )
            }
        }
    }
}
