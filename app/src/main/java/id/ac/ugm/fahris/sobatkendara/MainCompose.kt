package id.ac.ugm.fahris.sobatkendara

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import id.ac.ugm.fahris.sobatkendara.service.ApiService
import id.ac.ugm.fahris.sobatkendara.ui.components.AppDrawerContent
import id.ac.ugm.fahris.sobatkendara.ui.components.AppDrawerItemInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainCompose(
    navController: NavHostController = rememberNavController(),
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
) {
    val openAlertDialog = rememberSaveable { mutableStateOf(false) }
    Surface {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                AppDrawerContent(
                    drawerState = drawerState,
                    menuItems = DrawerParams.drawerButtons,
                    defaultPick = MainNavOption.DashboardScreen
                ) { onUserPickedOption ->
                    when (onUserPickedOption) {
                        MainNavOption.DashboardScreen -> {
                            navController.navigate(onUserPickedOption.name) {
                                popUpTo(NavRoutes.MainRoute.name)
                            }
                        }
                        MainNavOption.ConfigScreen -> {
                            navController.navigate(onUserPickedOption.name) {
                                popUpTo(NavRoutes.MainRoute.name)
                            }
                        }
                        MainNavOption.ChangePasswordScreen -> {
                            navController.navigate(onUserPickedOption.name) {
                                popUpTo(NavRoutes.MainRoute.name)
                            }
                        }
                        MainNavOption.AboutScreen -> {
                            navController.navigate(onUserPickedOption.name) {
                                popUpTo(NavRoutes.MainRoute.name)
                            }
                        }
                        MainNavOption.LogoutAction -> {
                            openAlertDialog.value = true
                        }
                    }
                }
            }
        ) {

            NavHost(
                navController,
                startDestination = NavRoutes.MainRoute.name
            ) {
                mainGraph(drawerState)
            }
        }
    }
    if (openAlertDialog.value) {
        AlertDialog(
            icon = {
                Icon(Icons.Default.Info, contentDescription = "Info Icon")
            },
            title = {
                Text(text = "Logout")
            },
            text = {
                Text(text = "Are you sure want to logout?")
            },
            onDismissRequest = {
                openAlertDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        openAlertDialog.value = false
                        CoroutineScope(Dispatchers.Main).launch {
                            val context = navController.context
                            val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
                            val token = sharedPreferences.getString("auth_token", null)
                            val message = ApiService.logout(context, token?: "",
                                onLogoutError = { err -> Toast.makeText(context, "Logout failed $err", Toast.LENGTH_SHORT).show() }
                            )
                            Log.d("Logout", "Message: $message")

                            sharedPreferences.edit().remove("auth_token").remove("account_email").apply()
                            val intent = Intent(context, LoginActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            context.startActivity(intent)
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        openAlertDialog.value = false
                    }
                ) {
                    Text("Dismiss")
                }
            }
        )
    }
}
enum class NavRoutes {
    MainRoute
}

object DrawerParams {
    val drawerButtons = arrayListOf(
        AppDrawerItemInfo(
            MainNavOption.DashboardScreen,
            R.string.drawer_dashboard,
            R.drawable.ic_dashboard_black_24dp,
            R.string.drawer_dashboard
        ),
        AppDrawerItemInfo(
            MainNavOption.ConfigScreen,
            R.string.drawer_config,
            R.drawable.ic_config_black_24dp,
            R.string.drawer_config
        ),
        AppDrawerItemInfo(
            MainNavOption.ChangePasswordScreen,
            R.string.drawer_change_password,
            R.drawable.ic_change_password_black_24dp,
            R.string.drawer_change_password
        ),
        AppDrawerItemInfo(
            MainNavOption.AboutScreen,
            R.string.drawer_about,
            R.drawable.ic_about_black_24dp,
            R.string.drawer_about
        ),
        AppDrawerItemInfo(
            MainNavOption.LogoutAction,
            R.string.drawer_logout,
            R.drawable.ic_logout_black_24dp,
            R.string.drawer_logout
        )
    )
}

@Preview
@Composable
fun MainActivityPreview() {
    MainCompose()
}