package id.ac.ugm.fahris.sobatkendara.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import id.ac.ugm.fahris.sobatkendara.R
import id.ac.ugm.fahris.sobatkendara.ui.components.AppBar

@Composable
fun ConfigScreen(
    drawerState: DrawerState,
) {
    Scaffold(
        topBar = {
            AppBar(
                drawerState = drawerState,
                title = R.string.drawer_config
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Config")
        }
    }
}

@Preview
@Composable
fun ConfigScreenPreview() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    ConfigScreen(drawerState)
}