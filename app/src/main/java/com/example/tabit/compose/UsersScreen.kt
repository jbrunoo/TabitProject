package com.example.tabit.compose

import android.util.Log
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
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.tabit.compose.MyBottomBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun UsersScreen(navController: NavController) {
    var documentIdList by remember { mutableStateOf<List<String>>(emptyList()) }
    var imageUrlList by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(Unit) {
        val db = Firebase.firestore

        // Fetch document IDs
        val tempDocumentIdList = mutableListOf<String>()
        val querySnapshot = db.collectionGroup("savedData").get().await()
        for (document in querySnapshot) {
            val documentId = document.id
            tempDocumentIdList.add(documentId)
        }
        documentIdList = tempDocumentIdList

        // Fetch image URLs
        val urls = mutableListOf<String>()
        for (documentId in documentIdList) {
            val docRef = db.collection("savedData").document(documentId)
            val document = docRef.get().await()
            if (document != null) {
                val imageUrl = document.getString("imageUrl")
                imageUrl?.let { urls.add(it) }
            }
        }
        imageUrlList = urls
    }

    Column {
        for (documentId in documentIdList) {
            Text(text = documentId)
        }
    }

// documentIdList에 있는 각각의 documentId에 있는 모든 imageUrlList을 imageUrlLists에 저장
    Column {
        for (imageUrl in imageUrlList) {
            Text(text = imageUrl)
        }
    }


// maxlinespan 이용한 lazyverticalgrid으로 반응형 만들기
    var selectedBarItem by remember { mutableIntStateOf(0) }
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
}


