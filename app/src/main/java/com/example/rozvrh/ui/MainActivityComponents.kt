package com.example.rozvrh.ui

import android.widget.CalendarView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.sharp.DateRange
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rozvrh.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeTableBar(
    title: String,
    onMenuClick: () -> Unit,
) {
    var showCalendar by remember { mutableStateOf(false) }
    val day = stringResource(R.string.monday)
    var selectedDay by remember { mutableStateOf(day) }
    val days = listOf(
        stringResource(R.string.monday),
        stringResource(R.string.tuesday),
        stringResource(R.string.wednesday),
        stringResource(R.string.thursday),
        stringResource(R.string.friday),
        stringResource(R.string.saturday),
        stringResource(R.string.sunday)
    )

    Column {
        TopAppBar(
            title = { Text(text = title) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            navigationIcon = {
                IconButton(onClick = { onMenuClick() }) {
                    Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.menu))
                }
            },
            actions = {
                IconButton(onClick = { showCalendar = true }) {
                    Icon(Icons.Sharp.DateRange,
                        contentDescription = stringResource(R.string.open_calendar))
                }
            }
        )

        if (showCalendar) {
           CalendarScreen(onDoneClick = { showCalendar = false }) // Dismiss calendar
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = MaterialTheme.colorScheme.primary)
        ) {
            items(days) { day ->
                Tab(
                    selected = selectedDay == day,
                    onClick = { selectedDay = day },
                    text = {
                        Text(
                            text = day,
                            textAlign = TextAlign.Center,
                            color = if (selectedDay == day) Color.White else Color.Black,
                            fontWeight = if (selectedDay == day) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun Subject(
    subjectName: String,
    startTime: String,
    endTime: String,
    additionalInfo: String,
    backgroundColor: Color = MaterialTheme.colorScheme.tertiary,
    cornerRadius: Int = 16
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
            )
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(cornerRadius.dp)
            )
            .padding(16.dp)  // Inner padding within the background
    ) {
        // Subject name
        Text(
            text = subjectName,
            style = MaterialTheme.typography.titleMedium,  // Text style for the subject name
            color = Color.White
        )

        // Start and end time
        Text(
            text = "Start: $startTime, End: $endTime",
            style = MaterialTheme.typography.bodyMedium,  // Text style for time
            color = Color.White
        )

        // Additional information
        Text(
            text = additionalInfo,
            style = MaterialTheme.typography.bodySmall,  // Text style for additional info
            color = Color.White
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onDoneClick: () -> Unit) {
    Column {
        // TopAppBar with Done button
        TopAppBar(
            title = { Text(text = "Select Date") },
            navigationIcon = {
                IconButton(onClick = { onDoneClick() }) {
                    Icon( Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                TextButton(onClick = { onDoneClick() }) {
                    Text("Done", color = Color.White)
                }
            }
        )

        // CalendarView from Android in Compose
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                CalendarView(context).apply {
                    // Optional: Set any initial settings on CalendarView here
                    setOnDateChangeListener { _, year, month, dayOfMonth ->
                        // Handle date change
                        val selectedDate = "$dayOfMonth/${month + 1}/$year"
                        // Optionally perform action with the selected date
                    }
                }
            }
        )
    }
}

@Composable
fun TimeTableScreen(
    onMenuClick: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TimeTableBar(
                title = stringResource(R.string.timeTableTitle),
                onMenuClick = onMenuClick,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(10) { _ ->
                Subject(
                    subjectName = "Mathematics",
                    startTime = "08:00",
                    endTime = "09:30",
                    additionalInfo = "Room 101, Lecture with Prof. Smith"
                )
            }
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubjectPreview() {
    MaterialTheme {
        Subject(
            subjectName = "Mathematics",
            startTime = "08:00",
            endTime = "09:30",
            additionalInfo = "Room 101, Lecture with Prof. Smith"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TimeTableScreenPreview() {
   TimeTableScreen()
}
