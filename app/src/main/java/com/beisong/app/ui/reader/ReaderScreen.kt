package com.beisong.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.beisong.app.data.CharInfo
import com.beisong.app.data.CharReading
import com.beisong.app.data.CharDefinition

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val currentText = uiState.segments.getOrElse(uiState.currentIndex) { "" }

    if (uiState.selectedChar != null) {
        CharLookupDialog(
            info = uiState.selectedChar!!,
            onDismiss = { viewModel.clearSelection() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.fileName) },
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
                isLookupMode = uiState.isLookupMode,
                onPrev = { viewModel.prevSegment() },
                onNext = { viewModel.nextSegment() },
                onToggleLookup = { viewModel.toggleLookupMode() }
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
            uiState.isLookupMode -> {
                LookupGrid(
                    text = currentText,
                    onCharClick = { viewModel.selectChar(it.toString()) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                )
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
                        text = currentText,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LookupGrid(
    text: String,
    onCharClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        for (char in text) {
            if (char.isWhitespace() || char.isISOControl()) continue
            Surface(
                onClick = { onCharClick(char) },
                shape = MaterialTheme.shapes.small,
                color = Color(0xFFB8E0BB),
                modifier = Modifier.widthIn(min = 48.dp)
            ) {
                Text(
                    text = char.toString(),
                    color = Color(0xFF333333),
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CharLookupDialog(
    info: CharInfo,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFC7EDCC),
        title = {
            Text(
                text = info.character,
                fontSize = 36.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF333333)
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                info.readings.forEach { reading ->
                    ReadingSection(reading)
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = Color(0xFF333333))
            }
        }
    )
}

@Composable
private fun ReadingSection(reading: CharReading) {
    Text(
        text = "【${reading.pinyin}】",
        fontSize = 18.sp,
        color = Color(0xFF555555)
    )
    Spacer(Modifier.height(4.dp))
    reading.definitions.forEachIndexed { index, def ->
        Text(
            text = "${index + 1}. ${def.meaning}",
            fontSize = 15.sp,
            color = Color(0xFF333333),
            lineHeight = 24.sp
        )
        def.details.forEach { detail ->
            val detailText = if (detail.book.isNotBlank()) {
                "  · ${detail.text} —《${detail.book}》"
            } else {
                "  · ${detail.text}"
            }
            Text(
                text = detailText,
                fontSize = 13.sp,
                color = Color(0xFF666666),
                lineHeight = 20.sp
            )
        }
        if (index < reading.definitions.size - 1) {
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun ReaderBottomBar(
    currentIndex: Int,
    totalSegments: Int,
    isLookupMode: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggleLookup: () -> Unit
) {
    Surface(
        color = Color(0xFFB8E0BB),
        tonalElevation = 2.dp,
        modifier = Modifier.navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
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

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.segment_info, currentIndex + 1, totalSegments),
                    color = Color(0xFF333333),
                    fontSize = 14.sp
                )
                Spacer(Modifier.width(12.dp))
                TextButton(onClick = onToggleLookup) {
                    Text(
                        text = if (isLookupMode) stringResource(R.string.read_mode) else stringResource(R.string.lookup_mode),
                        color = Color(0xFF333333),
                        fontSize = 14.sp
                    )
                }
            }

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
