package com.example.tabit.compose

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerScreen(navController: NavController, onSignOutClicked: () -> Unit) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
// icons to mimic drawer destinations
//    val iconitems = listOf(Default.Settings, Default., Default.Email)
//    val selectedIconItem = remember { mutableStateOf(iconitems[0]) }
    // onclick 안에 들어갈 내용 scope.launch { drawerState.close() }
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Column {
                        Row {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = "lock")
                            Text(text = "잠금화면")
                        }
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomStart)
                    ) {
                        Button(
                            onClick = { onSignOutClicked() },
                            colors = ButtonDefaults.buttonColors(Color.White, Color.Black),
                            border = BorderStroke(1.dp, Color.Black)
                        ) {
                            Text(text = "로그아웃")
                        }
                    }
                }
            }

        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { scope.launch { drawerState.open() } },
                    colors = ButtonDefaults.buttonColors(Color.White, Color.Black),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text("설정")
                }
            }
        }

    )
}
