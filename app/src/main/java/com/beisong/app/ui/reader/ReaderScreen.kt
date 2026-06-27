package com.beisong.app.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beisong.app.R
import com.beisong.app.data.CharInfo
import com.beisong.app.data.CharReading
import com.beisong.app.data.CharDefinition
import kotlin.math.roundToInt

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
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var selectedChar by remember { mutableStateOf("") }
    var showTranslateButton by remember { mutableStateOf(false) }
    var translateButtonPos by remember { mutableStateOf(Offset.Zero) }

    if (uiState.selectedChar != null) {
        CharLookupDialog(
            info = uiState.selectedChar!!,
            candidates = uiState.wordCandidates,
            onDismiss = {
                viewModel.clearSelection()
                selectedChar = ""
                showTranslateButton = false
            },
            onWordClick = { viewModel.onWordCandidateClicked(it) }
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
                    SelectionContainer(
                        modifier = Modifier.pointerInput(currentText) {
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitFirstDown(
                                        pass = PointerEventPass.Initial,
                                        requireUnconsumed = false
                                    )
                                    val downPos = down.position
                                    val downTime = System.nanoTime()

                                    while (true) {
                                        val event = awaitPointerEvent(PointerEventPass.Initial)
                                        val change = event.changes.firstOrNull() ?: break
                                        if (!change.pressed) break
                                    }

                                    val elapsed = (System.nanoTime() - downTime) / 1_000_000
                                    if (elapsed < viewConfiguration.longPressTimeoutMillis) {
                                        continue
                                    }

                                    val result = textLayoutResult ?: continue
                                    val charOffset = result.getOffsetForPosition(downPos)
                                    if (charOffset in currentText.indices) {
                                        val rect = result.getBoundingBox(charOffset)
                                        selectedChar = currentText[charOffset].toString()
                                        showTranslateButton = true
                                        translateButtonPos = Offset(
                                            rect.center.x,
                                            rect.bottom + 8.dp.toPx()
                                        )
                                    }
                                }
                            }
                        }
                    ) {
                        Text(
                            text = currentText,
                            color = Color(0xFF333333),
                            fontSize = 18.sp,
                            lineHeight = 32.sp,
                            textAlign = TextAlign.Start,
                            onTextLayout = { textLayoutResult = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (showTranslateButton && selectedChar.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(translateButtonPos.x.roundToInt(), translateButtonPos.y.roundToInt()) }
                        ) {
                            Card(
                                onClick = {
                                    viewModel.onTextSelected(selectedChar)
                                    showTranslateButton = false
                                },
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF333333))
                            ) {
                                Text(
                                    text = "翻译",
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharLookupDialog(
    info: CharInfo,
    candidates: List<String>,
    onDismiss: () -> Unit,
    onWordClick: (String) -> Unit
) {
    val scrollState = rememberScrollState()
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFC7EDCC),
        title = {
            Text(
                text = info.character,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF333333)
            )
        },
        text = {
            Column(modifier = Modifier.verticalScroll(scrollState)) {
                if (info.readings.isEmpty()) {
                    Text(
                        text = "未收录该字",
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    info.readings.forEach { reading ->
                        ReadingSection(reading)
                        Spacer(Modifier.height(10.dp))
                    }
                }
                if (candidates.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "词组",
                        fontSize = 14.sp,
                        color = Color(0xFF555555)
                    )
                    Spacer(Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        candidates.forEach { word ->
                            Surface(
                                onClick = { onWordClick(word) },
                                shape = MaterialTheme.shapes.small,
                                color = Color(0xFFB8E0BB)
                            ) {
                                Text(
                                    text = word,
                                    fontSize = 14.sp,
                                    color = Color(0xFF333333),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
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
        fontSize = 16.sp,
        color = Color(0xFF555555)
    )
    Spacer(Modifier.height(4.dp))
    reading.definitions.forEachIndexed { index, def ->
        Text(
            text = "${index + 1}. ${def.meaning}",
            fontSize = 14.sp,
            color = Color(0xFF333333),
            lineHeight = 22.sp
        )
        def.details.forEach { detail ->
            val detailText = if (detail.book.isNotBlank()) {
                "  · ${detail.text} —《${detail.book}》"
            } else {
                "  · ${detail.text}"
            }
            Text(
                text = detailText,
                fontSize = 12.sp,
                color = Color(0xFF666666),
                lineHeight = 18.sp
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
