package com.beisong.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beisong.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    fileName: String,
    onBack: () -> Unit,
    viewModel: ReaderViewModel = viewModel()
) {
    LaunchedEffect(fileName) {
        viewModel.loadFile(fileName)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { Text(uiState.fileName) } },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back), tint = Color(0xFF333333))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFC7EDCC),
                    titleContentColor = Color(0xFF333333)
                )
            )
        },
        bottomBar = {
            ReaderBottomBar(
                currentIndex = uiState.currentIndex,
                totalSegments = uiState.segments.size,
                onPrev = { viewModel.prevSegment() },
                onNext = { viewModel.nextSegment() }
            )
        },
        containerColor = Color(0xFFC7EDCC)
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF333333))
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(uiState.error ?: "", color = Color(0xFF666666), fontSize = 16.sp)
                }
            }
            uiState.segments.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_content), color = Color(0xFF666666), fontSize = 16.sp)
                }
            }
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFC7EDCC))
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    contentAlignment = Alignment.TopStart
                ) {
                    Text(
                        text = uiState.segments.getOrElse(uiState.currentIndex) { "" },
                        color = Color(0xFF333333),
                        fontSize = 18.sp,
                        lineHeight = 32.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderBottomBar(
    currentIndex: Int,
    totalSegments: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Surface(
        color = Color(0xFFB8E0BB),
        tonalElevation = 2.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = onPrev,
                enabled = currentIndex > 0
            ) {
                Text(
                    "< ${stringResource(R.string.prev_segment)}",
                    color = if (currentIndex > 0) Color(0xFF333333) else Color(0xFF999999),
                    fontSize = 14.sp
                )
            }

            Text(
                text = stringResource(R.string.segment_info, currentIndex + 1, totalSegments),
                color = Color(0xFF333333),
                fontSize = 14.sp
            )

            TextButton(
                onClick = onNext,
                enabled = currentIndex < totalSegments - 1
            ) {
                Text(
                    "${stringResource(R.string.next_segment)} >",
                    color = if (currentIndex < totalSegments - 1) Color(0xFF333333) else Color(0xFF999999),
                    fontSize = 14.sp
                )
            }
        }
    }
}
