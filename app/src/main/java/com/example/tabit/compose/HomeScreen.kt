package com.example.tabit.compose

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.traceEventEnd
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tabit.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


@Composable
fun HomeScreen(navController: NavController) {
    var selectedBarItem by remember { mutableIntStateOf(0) }
    var showExitDialog by remember { mutableStateOf(false) }
    // firestore imageUrl 가져오기 (imageUrl을 넣어야 이미지 표시하는 만큼만 가져와서 효율적)
    val uid = remember { FirebaseAuth.getInstance().currentUser?.uid }
    var imageUrlList = ImageUrlListProvider(uid = uid)

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 56.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Tabit",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                )
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    columns = GridCells.Fixed(3),
                ) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .size(150.dp)
                                .padding(2.dp)
                                .clickable { navController.navigate("add") },
                            shape = RectangleShape
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add, contentDescription = "Add",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    items(imageUrlList) { imageUrl ->
                        PhotoCard(imageUrl, navController)
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            MyBottomBar(
                navController,
                selectedBarItem
            ) { newIndex ->
                selectedBarItem = newIndex
            }
        }
    }
    BackHandler {
        // 홈 화면에서 뒤로가기 버튼을 눌렀을 때
        showExitDialog = true
    }
    if (showExitDialog) {
        // 함수를 바로 BackHandler 안에 썼을 때, @Composable invocations can only happen from the context of a @Composable function
        // 버튼의 onclick 안에서도 composable 함수를 쓸 수 없던데 이렇게 해결해야 하는건지? (상태 관리)
        ShowExitConfirmationDialog(navController)
    }
}

@Composable
fun ShowExitConfirmationDialog(navController: NavController) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = "앱 종료") },
        text = { Text(text = "앱을 종료하시겠습니까?") },
        confirmButton = {
            TextButton(
                onClick = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
//                    navController.previousBackStackEntry?.activity?.finish()
                    navController.popBackStack()
                }
            ) {
                Text(text = "확인")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    navController.navigate(Screen.Home.route)
                }
            ) {
                Text(text = "취소")
            }
        }
    )
}