package com.noryangjin.simplesignage

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import kotlin.math.min

@SuppressLint("NewApi")
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val today = LocalDate.now()
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf(today) }
    var focusedDate by remember { mutableStateOf(today) }

    LaunchedEffect(Unit) {
        focusedDate = today
        selectedDate = today
        currentYearMonth = YearMonth.from(today)
    }

    val holidays = remember {
        mapOf(
            "2024-01-01" to "신정",
            "2024-02-09" to "설날",
            "2024-02-10" to "설날",
            "2024-02-11" to "설날",
            "2024-03-01" to "삼일절",
            "2024-04-10" to "21대 총선",
            "2024-05-05" to "어린이날",
            "2024-05-15" to "부처님오신날",
            "2024-06-06" to "현충일",
            "2024-08-15" to "광복절",
            "2024-09-16" to "추석",
            "2024-09-17" to "추석",
            "2024-09-18" to "추석",
            "2024-10-03" to "개천절",
            "2024-10-09" to "한글날",
            "2024-12-25" to "크리스마스"
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Top
        ) {
            // 월 표시
            Text(
                text = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월",
                style = MaterialTheme.typography.headlineLarge,  // 글자 크기 증가
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 요일 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.titleLarge,  // 글자 크기 증가
                        color = when (day) {
                            "일" -> Color.Red
                            "토" -> Color.Blue
                            else -> Color.Black
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 달력 그리드
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // 달력 데이터 준비
                    val weeks = mutableListOf<List<LocalDate>>()
                    val firstDayOfMonth = currentYearMonth.atDay(1)
                    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

                    // 첫 주 시작일 계산
                    var currentDate = firstDayOfMonth.minusDays(firstDayOfWeek.toLong())

                    // 5주 분량의 날짜 생성
                    repeat(5) { weekIndex ->
                        val week = mutableListOf<LocalDate>()
                        repeat(7) { dayIndex ->
                            week.add(currentDate)
                            currentDate = currentDate.plusDays(1)
                        }
                        weeks.add(week)
                    }

                    // 각 주를 Row로 표시
                    weeks.forEach { week ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            week.forEach { date ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    CalendarDay(
                                        date = date,
                                        isSelected = date == selectedDate,
                                        isFocused = date == focusedDate,
                                        isCurrentMonth = date.monthValue == currentYearMonth.monthValue,
                                        holidays = holidays,
                                        currentYearMonth = currentYearMonth,
                                        onDateSelected = {
                                            selectedDate = date
                                            focusedDate = date
                                            if (date.monthValue != currentYearMonth.monthValue) {
                                                currentYearMonth = YearMonth.from(date)
                                            }
                                        },
                                        onMonthChange = { change ->
                                            currentYearMonth = currentYearMonth.plusMonths(change.toLong())
                                            val newDate = currentYearMonth.atDay(min(currentYearMonth.lengthOfMonth(), date.dayOfMonth))
                                            selectedDate = newDate
                                            focusedDate = newDate
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


@SuppressLint("NewApi")
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CalendarDay(
    date: LocalDate,
    isSelected: Boolean,
    isFocused: Boolean,
    currentYearMonth: YearMonth,
    isCurrentMonth: Boolean,
    holidays: Map<String, String>,
    onDateSelected: () -> Unit,
    onMonthChange: (Int) -> Unit,
    enabled: Boolean = true,

) {
    val dateString = "${date.year}-${String.format("%02d", date.monthValue)}-${String.format("%02d", date.dayOfMonth)}"
    val isHoliday = holidays.containsKey(dateString)
    val isWeekend = date.dayOfWeek.value == 6 || date.dayOfWeek.value == 7

    val today = LocalDate.now()
    val isToday = date.equals(today)

    Button(
        onClick = onDateSelected,
        enabled = enabled,
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
            .onFocusChanged { focusState ->
                if (focusState.isFocused && date.monthValue == currentYearMonth.monthValue) {
                    onDateSelected()
                }
            }
            .onPreviewKeyEvent { keyEvent ->
                when {
                    ((date.dayOfMonth <= 7 && keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP) ||
                            (date.dayOfMonth == 1 && keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT)) -> {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            onMonthChange(-1)
                            true
                        } else false
                    }
                    ((date.dayOfMonth > date.lengthOfMonth() - 7 && keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN) ||
                            (date.dayOfMonth == date.lengthOfMonth() && keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT)) -> {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            onMonthChange(1)
                            true
                        } else false
                    }
                    else -> false
                }
            }
        ,
        shape = ButtonDefaults.shape(
            shape = RoundedCornerShape(4.dp)
        ),
        colors = ButtonDefaults.colors(
            containerColor = when {
                isToday -> Color.Gray.copy(alpha = 0.3f)
                !isCurrentMonth -> Color.Transparent
                else -> Color.Transparent
            },
            contentColor = when {
                !isCurrentMonth -> Color.Gray
                isHoliday -> Color.Red
                isWeekend && date.dayOfWeek.value == 7 -> Color.Red
                isWeekend && date.dayOfWeek.value == 6 -> Color.Blue
                else -> Color.Black
            },
            focusedContainerColor = Color(0xFF87CEEB).copy(alpha = 0.3f)
        ),
        scale = ButtonDefaults.scale(
            focusedScale = 1.05f
        )
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = when {
                !enabled -> Color.Gray.copy(alpha = 0.5f)
                !isCurrentMonth -> Color.Gray
                isHoliday -> Color.Red
                isWeekend && date.dayOfWeek.value == 7 -> Color.Red
                isWeekend && date.dayOfWeek.value == 6 -> Color.Blue
                else -> Color.Black
            }
        )
    }
}