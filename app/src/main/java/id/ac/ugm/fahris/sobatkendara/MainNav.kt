package id.ac.ugm.fahris.sobatkendara

import androidx.compose.material3.DrawerState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import id.ac.ugm.fahris.sobatkendara.ui.AboutScreen
import id.ac.ugm.fahris.sobatkendara.ui.ChangePasswordScreen
import id.ac.ugm.fahris.sobatkendara.ui.ConfigScreen
import id.ac.ugm.fahris.sobatkendara.ui.DashboardScreen

fun NavGraphBuilder.mainGraph(drawerState: DrawerState) {
    navigation(startDestination = MainNavOption.DashboardScreen.name, route = NavRoutes.MainRoute.name) {
        composable(MainNavOption.DashboardScreen.name){
            DashboardScreen(drawerState)
        }
        composable(MainNavOption.ConfigScreen.name){

            ConfigScreen(drawerState)
        }
        composable(MainNavOption.ChangePasswordScreen.name){
            ChangePasswordScreen(drawerState)
        }
        composable(MainNavOption.AboutScreen.name){
            AboutScreen(drawerState)
        }
    }
}

enum class MainNavOption {
    DashboardScreen,
    ConfigScreen,
    ChangePasswordScreen,
    AboutScreen,
    LogoutAction
}