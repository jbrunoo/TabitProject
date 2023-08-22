package com.example.tabit.compose

import android.media.Image
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@Composable
fun DetailScreen(navController: NavController, imageUrl: String) {
    val todoListState = remember { mutableStateOf<List<String>>(emptyList()) }
    val db = Firebase.firestore
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    var isChecked by remember { mutableStateOf(false) }
    var dateFromFirestore by remember { mutableStateOf<String>("") }
    val context = LocalContext.current

    imageUrl.let { url ->
        LaunchedEffect(url) {
            if (uid != null) {
                val savedDataCollectionRef = db.collection("users").document(uid)
                    .collection("savedData")
                val querySnapshot = savedDataCollectionRef
                    .whereEqualTo("imageUrl", url)
                    .get().await()

                val document = querySnapshot.documents.firstOrNull()
                if (document != null) {
                    val todoList = document.get("toDoList") as? List<String>
                    if (todoList != null) {
                        todoListState.value = todoList
                    }
                }
                dateFromFirestore = document?.get("date") as? String ?: ""
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, bottom = 24.dp, start = 30.dp, end = 30.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = dateFromFirestore,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                IconButton(onClick = {
                    if (uid != null) {
                        db.collection("users").document(uid)
                            .collection("savedData")
                            .whereEqualTo("imageUrl", imageUrl)
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                for (document in querySnapshot) {
                                    document.reference.delete()
                                }
                                Toast.makeText(context, "삭제 성공", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                // Handle failure, such as showing an error message
                                Toast.makeText(context, "삭제 실패", Toast.LENGTH_SHORT).show()
                            }
                        navController.popBackStack()
                    }
                }) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "delete")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            // ToDo 목록 표시
            todoListState.value.forEach { todo ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (todo != "") {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { isChecked = it },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Text(text = todo)
                }
            }
        }
    }
}

