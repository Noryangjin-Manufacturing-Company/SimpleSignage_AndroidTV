package com.noryangjin.simplesignage

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth

@SuppressLint("NewApi")
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val today = LocalDate.now()
    var currentYearMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf(today) }
    var focusedDate by remember { mutableStateOf(today) }

    val initialFocusRequester = remember { FocusRequester() }
    val monthChangeFocusRequester = remember { FocusRequester() }

    // 초기 포커스 설정
    LaunchedEffect(Unit) {
        delay(10)
        initialFocusRequester.requestFocus()
    }

    // 달 변경 시 포커스 설정
    LaunchedEffect(focusedDate) {
        if (focusedDate != today) {  // 초기 로딩이 아닐 때만
            delay(10)  // 리컴포지션 완료를 위한 딜레이
            monthChangeFocusRequester.requestFocus()
        }
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
            Text(
                text = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val daysOfWeek = listOf("일", "월", "화", "수", "목", "금", "토")
                daysOfWeek.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.titleLarge,
                        color = when (day) {
                            "일" -> Color.Red
                            "토" -> Color.Blue
                            else -> Color.Black
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    val firstDayOfMonth = currentYearMonth.atDay(1)
                    val lastDayOfMonth = currentYearMonth.atEndOfMonth()
                    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

                    val totalDays = firstDayOfWeek + lastDayOfMonth.dayOfMonth
                    val numberOfWeeks = ((totalDays + 6) / 7)

                    var currentDate = firstDayOfMonth.minusDays(firstDayOfWeek.toLong())

                    val weeks = mutableListOf<List<LocalDate>>()
                    repeat(numberOfWeeks) {
                        val week = mutableListOf<LocalDate>()
                        repeat(7) {
                            week.add(currentDate)
                            currentDate = currentDate.plusDays(1)
                        }
                        weeks.add(week)
                    }

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
                                        onDateChange = { date, direction, keyEvent ->
                                            val newYearMonth = when (direction) {
                                                DateChangeDirection.PREV -> currentYearMonth.minusMonths(1)
                                                DateChangeDirection.NEXT -> currentYearMonth.plusMonths(1)
                                            }

                                            val dayOfWeek = date.dayOfWeek  // 현재 요일 저장

                                            val newDate = when {
                                                // DPAD_LEFT: 1일에서 이전 달 말일로
                                                keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT &&
                                                        date.dayOfMonth == 1 -> {
                                                    newYearMonth.atEndOfMonth()
                                                }
                                                // DPAD_RIGHT: 말일에서 다음 달 1일로
                                                keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT &&
                                                        date.dayOfMonth == date.lengthOfMonth() -> {
                                                    newYearMonth.atDay(1)
                                                }
                                                // DPAD_UP: 첫 주에서 이전 달의 같은 요일로
                                                keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP &&
                                                        date.dayOfMonth <= 7 -> {
                                                    val lastDayOfPrevMonth = newYearMonth.atEndOfMonth()
                                                    var targetDate = lastDayOfPrevMonth.minusDays(7)  // 마지막 주가 아닌 그 전 주의 같은 요일
                                                    while (targetDate.dayOfWeek != dayOfWeek) {
                                                        targetDate = targetDate.plusDays(1)
                                                    }
                                                    targetDate
                                                }
                                                // DPAD_DOWN: 마지막 주나 말일에서 다음 달의 같은 요일로
                                                ((keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN &&
                                                        date.dayOfMonth > date.lengthOfMonth() - 7) ||
                                                        (keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN &&
                                                                date.dayOfMonth == date.lengthOfMonth())) -> {
                                                    var targetDate = newYearMonth.atDay(1)
                                                    while (targetDate.dayOfWeek != dayOfWeek) {
                                                        targetDate = targetDate.plusDays(1)
                                                    }
                                                    targetDate
                                                }
                                                else -> date
                                            }

                                            currentYearMonth = newYearMonth
                                            selectedDate = newDate
                                            focusedDate = newDate
                                        },
                                        initialFocusRequester = if (date == today) initialFocusRequester else null,
                                        monthChangeFocusRequester = if (date == focusedDate) monthChangeFocusRequester else null
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

enum class DateChangeDirection {
    PREV, NEXT
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
    onDateChange: (LocalDate, DateChangeDirection, KeyEvent) -> Unit,
    enabled: Boolean = true,
    initialFocusRequester: FocusRequester? = null,
    monthChangeFocusRequester: FocusRequester? = null
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
            .let {
                when {
                    initialFocusRequester != null -> it.focusRequester(initialFocusRequester)
                    monthChangeFocusRequester != null -> it.focusRequester(monthChangeFocusRequester)
                    else -> it
                }
            }
            .onFocusChanged { focusState ->
                if (focusState.isFocused && isCurrentMonth) {
                    onDateSelected()
                }
            }
            .focusProperties {
                left = FocusRequester.Default
                right = FocusRequester.Default
                up = FocusRequester.Default
                down = FocusRequester.Default
            }
            .onPreviewKeyEvent { keyEvent ->
                when {
                    keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP &&
                            date.dayOfMonth <= 7 -> {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            onDateChange(date, DateChangeDirection.PREV, keyEvent)
                            true
                        } else false
                    }
                    keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN &&
                            date.dayOfMonth > date.lengthOfMonth() - 7 -> {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            onDateChange(date, DateChangeDirection.NEXT, keyEvent)
                            true
                        } else false
                    }
                    keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT &&
                            date.dayOfMonth == 1 -> {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            onDateChange(date, DateChangeDirection.PREV, keyEvent)
                            true
                        } else false
                    }
                    keyEvent.nativeKeyEvent.keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT &&
                            date.dayOfMonth == date.lengthOfMonth() -> {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            onDateChange(date, DateChangeDirection.NEXT, keyEvent)
                            true
                        } else false
                    }
                    else -> false
                }
            },
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