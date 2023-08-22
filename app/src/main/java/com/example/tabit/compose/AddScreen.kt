package com.example.tabit.compose

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.tabit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(navController: NavController) {
    var selectUri by remember { // 갤러리 이미지 uri 객체
        mutableStateOf<Uri?>(null)
    }
    var takenPhoto by remember { // 기본 사진 앱 비트맵 객체
        mutableStateOf<Bitmap?>(null)
    }
    val context = LocalContext.current
    val launcher = // 갤러리 이미지 런쳐
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            onResult = { uri ->
                selectUri = uri
                takenPhoto = null
            }
        )
    val cameraLauncher = // 카메라 이미지 런쳐
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview(),
            onResult = { photo ->
                takenPhoto = photo
                selectUri = null
            })
    var uploadedImageBitmap by remember { mutableStateOf<Bitmap?>(null) } // processedImage에서 반환되는 값
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) } // url을 string으로 받기 위한 변수 초기화
    // 날짜 부분 변수들
    val currentDate = LocalDate.now()
    val oneYearAgoDate = currentDate.minusYears(1)
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var inputDate by remember { mutableStateOf(selectedDate.toString()) }
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    // 할 일 textfield 값들 리스트로 받음
    val toDoList = remember { mutableStateListOf<String>("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp, bottom = 24.dp, start = 30.dp, end = 30.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                text = "새 테이블 추가",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(4.dp)
            )
            Row(
                modifier = Modifier
                    .padding(top = 32.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    ProcessedImage( // 함수로 관리 uri, bitmap, 기본 사진
                        context = context,
                        uri = selectUri,
                        takenPhoto = takenPhoto,
                        isDefault = selectUri == null && takenPhoto == null
                    ) { bitmap ->
                        uploadedImageBitmap = bitmap
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(150.dp),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(onClick = {
                        launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.gallery),
                            contentDescription = null
                        )
                    }
                    IconButton(onClick = {
                        cameraLauncher.launch(null)  // 기본 카메라 앱 실행
                    }) {
                        Icon(painterResource(id = R.drawable.camera), contentDescription = null)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "날짜",
                fontWeight = FontWeight.ExtraBold
            )
            OutlinedTextField(
                value = inputDate,
                onValueChange = { inputDate = it },
                label = { Text(text = "date") },
                placeholder = { Text(text = "yyyy-mm-dd") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (toDoList.size < 5) {
                            toDoList.add("")
                        } else {
                            Toast
                                .makeText(context, "최대 5개입니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "할 일",
                    fontWeight = FontWeight.ExtraBold
                )
                Row {
                    IconButton(onClick = {
                        if (toDoList.size < 5) {
                            toDoList.add("")
                        } else {
                            Toast
                                .makeText(context, "최대 5개입니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Delete")
                    }
                    IconButton(onClick = {
                        if (toDoList.size > 1) {
                            toDoList.removeAt(toDoList.size - 1)
                        } else {
                            Toast
                                .makeText(context, "최소 1개입니다.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            for (i in 0 until toDoList.size) {
                OutlinedTextField(
                    value = toDoList[i], onValueChange = { toDoList[i] = it },
                    label = { Text(text = "to-do") },
                    placeholder = { Text(text = "할 일을 입력하세요.") },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            // 체크박스 선택시 completed 값을 false true로 바꿔주면 되려나?
        }
        Box(
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Button(
                onClick = {
                    try { // 날짜 format에 맞는지 예외처리
                        val parsedDate = LocalDate.parse(inputDate, dateFormatter)
                        if (parsedDate.isAfter(currentDate) || parsedDate.isBefore(oneYearAgoDate)) {
                            Toast.makeText(context, "1년 전까지 입력가능합니다.", Toast.LENGTH_SHORT).show()
                        }
                        selectedDate = parsedDate
                        // 날짜 값이 올바를 때, 이미지 저장하기
                    } catch (e: Exception) {
                        Toast.makeText(context, "yyyy-mm-dd로 입력하세요", Toast.LENGTH_SHORT).show()
                    }
                    // 이미지 storage에 업로드 작업
                    uploadedImageBitmap?.let { bitmap ->
                        val storageRef = Firebase.storage.reference
                        val imagesRef = storageRef.child("images/${System.currentTimeMillis()}.jpg")
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        val data = baos.toByteArray()
                        val uploadTask = imagesRef.putBytes(data)
                        uploadTask.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                imagesRef.downloadUrl.addOnSuccessListener { uri ->
                                    uploadedImageUrl = uri.toString()
                                    // firebase에 이미지 url과 to-do list의 내용을 저장하는 부분
                                    // onSuccessListener에 넣지 않으면 비동기 작업이기 때문에 이미지가 업로드 되기 전, imageUrl을 가져오기 때문에 null 값이 출력되었음.
                                    // cf. 그럼 업로드까지 기다리기 위해 await()을 쓰면 되지 않나? 라이브러리 추가해서 더 간결한 코드 쓸 수 있을 듯.
                                    val db = Firebase.firestore
                                    val uid = FirebaseAuth.getInstance().currentUser?.uid

                                    if (uid != null) {
                                        val savedDataCollectionRef =
                                            db.collection("users").document(uid)
                                                .collection("savedData")
                                        val imageUrl = uploadedImageUrl
                                        val newData = hashMapOf(
                                            "imageUrl" to imageUrl,
                                            "date" to selectedDate.toString() // 날짜 부분도 db 저장
                                        )
                                        savedDataCollectionRef.add(newData)
                                            .addOnSuccessListener { documentReference ->
                                                val newDataId = documentReference.id
                                                // 이미지 데이터 문서 ID를 사용하여 to-do 목록을 업데이트할 수 있음
                                                val newToDoList = toDoList.toList()
                                                savedDataCollectionRef.document(newDataId)
                                                    .update("toDoList", newToDoList)
                                                    .addOnSuccessListener {
                                                        Toast.makeText(
                                                            context,
                                                            "저장 성공",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                        // 이미지와 to-do 목록 업데이트 성공 처리
                                                    }
                                                    .addOnFailureListener {
                                                        Toast.makeText(
                                                            context,
                                                            "저장 실패",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    context,
                                                    "이미지 저장 실패",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }

                                    navController.navigate("home")
                                }
                            }
                        }
                    }

                },
                colors = ButtonDefaults.buttonColors(Color.White, Color.Black),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Text(text = "저장")
            }
        }
    }
}

@Composable
fun ProcessedImage( // 갤러리 이미지 선택 후 갤러리 이미지 취소하면 기본 desk가 아닌 갤러리 이미지가 남아있음 => launcher 부분에 각 이미지 가져올 때 다른 이미지 초기화 선언
    context: Context,
    uri: Uri?,
    takenPhoto: Bitmap?,
    isDefault: Boolean,
    onImageProcessed: (Bitmap?) -> Unit
) {
    val bitmap: Bitmap? =
        when { // bitmap 변수에 갤러리 이미지(uri), 카메라 사진(bitmap) 중 null이 아닌 값을 저장, 둘 다 nul이면 기본 desk(png) 저장
            uri != null -> {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        context.contentResolver,
                        uri
                    )
                )
            }

            takenPhoto != null -> takenPhoto
            isDefault -> BitmapFactory.decodeResource(context.resources, R.drawable.desk)
            else -> null
        }
    bitmap?.let {// imagebitmap으로 바꿔주는 과정.
        Image(
            bitmap = it.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(150.dp)
                .border(BorderStroke(1.dp, Color.Gray))
        )
    }
    onImageProcessed(bitmap)
}