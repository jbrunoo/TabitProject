package com.example.tabit.compose

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

@Composable
fun TrackerScreen(navController: NavController) {
    var selectedBarItem by remember { mutableIntStateOf(0) }
    // 캘린더 구현 및 to-do list에 맞는 dot 찍기
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) } // Adjust as needed
    val endMonth = remember { currentMonth.plusMonths(100) } // Adjust as needed
    val daysOfWeek = remember { daysOfWeek() }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    // firestore에서 날짜에 저장한 to-do list 불러오기
    val db = Firebase.firestore
    val uid = FirebaseAuth.getInstance().currentUser?.uid

    // 선택한 날짜에 해당하는 to-do 목록 가져오기
    var selectedToDoList: List<String> by remember(selectedDate) {
        mutableStateOf(emptyList())
    }

    LaunchedEffect(selectedDate, db, uid) {
        if (selectedDate != null && uid != null) {
            val savedDataCollectionRef = db.collection("users").document(uid)
                .collection("savedData")

            val formattedDate = selectedDate.toString()
            savedDataCollectionRef
                .whereEqualTo("date", formattedDate)
                .get()
                .addOnSuccessListener { documents ->
                    val document = documents.firstOrNull()
                    selectedToDoList = document?.get("toDoList") as? List<String> ?: emptyList()
                }
                .addOnFailureListener { exception ->
                    Log.e("Firestore", "Error getting documents: ", exception)
                }
        }
    }

    Box {
        Column {
            Spacer(modifier = Modifier.height(40.dp))
            HorizontalCalendar(
                state = state,
                dayContent = { day ->
                    Day(day, isSelected = selectedDate == day.date) { day ->
                        selectedDate = if (selectedDate == day.date) null else day.date
                    }
                },
                monthHeader = { month ->
                    val currentMonthName = month.yearMonth.month.getDisplayName(
                        java.time.format.TextStyle.FULL,
                        Locale.getDefault()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = currentMonthName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    DaysOfWeekTitle(daysOfWeek = daysOfWeek) // Use the title as month header
                }
            )
            Box(
                modifier = Modifier
                    .padding(start = 50.dp, end = 50.dp)
                    .fillMaxWidth()
            ) {
                if (selectedDate != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (selectedToDoList.isNotEmpty()) {
                        Column {
                            Row(horizontalArrangement = Arrangement.Center) {
                                Text(text = "to-do list", fontWeight = FontWeight.Bold)
                            }
                            for (toDoItem in selectedToDoList) {
                                Text(text = "- $toDoItem")
                            }
                        }
                    } else {
                        Text(text = "목록이 없습니다.")
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

@Composable
fun Day(day: CalendarDay, isSelected: Boolean, onClick: (CalendarDay) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // This is important for square sizing!
            .clip(CircleShape)
            .background(color = if (isSelected) Color.Green else Color.Transparent)
            .clickable(
                enabled = day.position == DayPosition.MonthDate,
                onClick = { onClick(day) }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            color = if (day.position == DayPosition.MonthDate) Color.Black else Color.Gray
        )
    }
}

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                text = dayOfWeek.getDisplayName(
                    java.time.format.TextStyle.SHORT,
                    Locale.getDefault()
                ),
            )
        }
    }
}