package com.example.tabit.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.tabit.Screen
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MyBottomBar(
    navController: NavController,
    selectedBarItem: Int,
    onItemSelected: (Int) -> Unit, // state hoisting
) {
    BottomNavigation(
        backgroundColor = Color.White
    ) {
        listOf("메뉴", "사람", "달력", "나").forEachIndexed { index, item ->
            val icon = when (index) {
                0 -> Icons.Default.Menu
                1 -> Icons.Default.Face
                2 -> Icons.Default.DateRange
                3 -> Icons.Default.Home
                else -> Icons.Default.Warning
            }
            BottomNavigationItem(
                icon = { Icon(icon, contentDescription = item) },
                label = { Text(item) },
                selected = selectedBarItem == index,
                onClick = {
                    onItemSelected(index)
                    navController.navigate(getRouteForScreenIndex(index)) {
                        popUpTo(getRouteForScreenIndex(index)) { inclusive =true }
                    }
                }
            )
        }
    }
}

private fun getRouteForScreenIndex (index: Int): String {
    val selectedScreen = when (index) {
        0 -> Screen.Drawer
        1 -> Screen.Users
        2 -> Screen.Tracker
        3 -> Screen.Home
        else -> throw IllegalArgumentException("Invalid screen index")
    }
    return selectedScreen.route
}

@Composable
fun ImageUrlListProvider(uid: String?): List<String> {
    val db = Firebase.firestore
    val imageUrlList = remember { mutableStateListOf<String>() }
    //        MutableStateFlow<List<String>>(emptyList())

    LaunchedEffect(uid, db) {// imageUrlList를 가져오는 비동기 작업을 실행
        if (uid != null) {
            val savedDataCollectionRef = db.collection("users").document(uid).collection("savedData")
            savedDataCollectionRef.get().addOnSuccessListener { querySnapshot ->
                val urls = mutableListOf<String>()

                for (document in querySnapshot) {
                    val imageUrl = document.getString("imageUrl")
                    if (imageUrl != null) {
                        urls.add(imageUrl)
                    }
                }
                imageUrlList.clear()
                imageUrlList.addAll(urls)
//                imageUrlListFlow.value = urls

            }
        }
    }

    return imageUrlList
}

@Composable
fun PhotoCard(imageUrl: String, navController: NavController) {
    Card(
        modifier = Modifier
            .fillMaxSize()
            .size(150.dp)
            .padding(2.dp)
            .clickable {
                val encodedUrl = URLEncoder.encode(imageUrl, StandardCharsets.UTF_8.toString())
                navController.navigate("detail/$encodedUrl")
            },
        shape = RectangleShape
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
//            rememberImagePainter은 coil 라이브러리 함수, coil(Coroutine Image Loader) 코루틴 내제
//            val painter: Painter = rememberAsyncImagePainter(imageUrl) 안됐음 공식문서보니 AsyncImage로 가능.
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
