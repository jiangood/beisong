# Long-press Lookup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current tap-to-lookup mode with long-press selection on reading text. Selected character's dictionary entry appears in a popup with word candidates.

**Architecture:** Compose `SelectionContainer` wraps the reading text. `onTextLayout` + `onSelectionChanged` callbacks detect selection and trigger ViewModel lookup. Word candidates are computed from segment text around the selected character.

**Tech Stack:** Jetpack Compose, Material3, Android Kotlin

---

### Task 1: Update ReaderUiState and ViewModel

**Files:**
- Modify: `app/src/main/java/com/beisong/app/ui/reader/ReaderViewModel.kt:18-27`

- [ ] **Step 1: Remove `isLookupMode`, add `wordCandidates` to ReaderUiState**

Replace `isLookupMode: Boolean` with `wordCandidates: List<String>`. The popup now shows these word candidates alongside the dictionary entry.

```kotlin
data class ReaderUiState(
    val fileName: String = "",
    val rawFileName: String = "",
    val segments: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null,
    val wordCandidates: List<String> = emptyList(),
    val selectedChar: CharInfo? = null
)
```

- [ ] **Step 2: Add `onTextSelected(text: String)` method**

Extracts the first CJK character from selected text, looks it up, computes word candidates from the current segment:

```kotlin
fun onTextSelected(text: String) {
    val char = text.firstOrNull { it in '\u4e00'..'\u9fff' || it in '\u3400'..'\u4dbf' }?.toString() ?: return
    val found = charDict.lookup(char)
    val segment = _uiState.value.segments.getOrElse(_uiState.value.currentIndex) { "" }
    val idx = segment.indexOf(char)
    val candidates = mutableListOf<String>()
    if (idx >= 0) {
        if (idx + 1 < segment.length && segment[idx + 1] in '\u4e00'..'\u9fff') {
            candidates.add(char + segment[idx + 1])
        }
        if (idx > 0 && segment[idx - 1] in '\u4e00'..'\u9fff') {
            candidates.add(segment[idx - 1].toString() + char)
        }
        if (idx > 0 && idx + 1 < segment.length &&
            segment[idx - 1] in '\u4e00'..'\u9fff' && segment[idx + 1] in '\u4e00'..'\u9fff') {
            candidates.add(segment[idx - 1].toString() + char + segment[idx + 1])
        }
    }
    _uiState.update {
        it.copy(
            wordCandidates = candidates,
            selectedChar = found ?: CharInfo(char, emptyList())
        )
    }
}
```

- [ ] **Step 3: Remove `toggleLookupMode()` and update `clearSelection()`**

```kotlin
fun clearSelection() {
    _uiState.update { it.copy(selectedChar = null, wordCandidates = emptyList()) }
}
```

Delete the entire `toggleLookupMode()` method.

- [ ] **Step 4: Update `nextSegment`/`prevSegment` to also clear `wordCandidates`**

Change `selectedChar = null` to `selectedChar = null, wordCandidates = emptyList()` in both methods.

- [ ] **Step 5: Build and verify**

Run: `.\gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

---

### Task 2: Rewrite ReaderScreen — wrap text in SelectionContainer, remove LookupGrid

**Files:**
- Modify: `app/src/main/java/com/beisong/app/ui/reader/ReaderScreen.kt`

- [ ] **Step 1: Remove LookupGrid composable entirely**

Delete the `LookupGrid` function and its `@OptIn(ExperimentalLayoutApi::class)`. Also remove the unused `clickable` import if it remains.

- [ ] **Step 2: Wrap reading Text in SelectionContainer with onSelectionChanged**

Replace the reading mode Box (the else branch, lines 109-127) with:

```kotlin
SelectionContainer {
    Text(
        text = currentText,
        color = Color(0xFF333333),
        fontSize = 18.sp,
        lineHeight = 32.sp,
        textAlign = TextAlign.Start,
        onTextLayout = { textLayoutResult ->
            // Store layout result for selection handling
        }
    )
}
```

But actually `SelectionContainer` + `onSelectionChanged` is the right approach. Replace the else branch:

```kotlin
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
        SelectionContainer {
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
```

- [ ] **Step 3: Remove isLookupMode branching from the Scaffold body**

Replace the `when` block to just have `isLoading`, `error`, `isEmpty`, and the default reading mode. Remove the `uiState.isLookupMode -> LookupGrid(...)` branch.

```kotlin
when {
    uiState.isLoading -> { ... loading box ... }
    uiState.error != null -> { ... error box ... }
    uiState.segments.isEmpty() -> { ... empty box ... }
    else -> {
        Box(...) {
            SelectionContainer {
                Text(...)
            }
        }
    }
}
```

- [ ] **Step 4: Update bottom bar — remove lookup toggle button**

Remove the `TextButton(onClick = onToggleLookup)` block. Keep only prev/next nav and segment counter.

Remove these parameters from `ReaderBottomBar` signature: `isLookupMode`, `onToggleLookup`. Update the call site in `ReaderScreen` accordingly.

- [ ] **Step 5: Shrink CharLookupDialog fonts**

Update the `CharLookupDialog`:
- Title: 24sp (was 36sp)
- Pinyin heading in `ReadingSection`: 16sp (was 18sp)
- Definition text: 14sp (was 15sp)
- Detail citations: 12sp (was 13sp)

- [ ] **Step 6: Add word candidates section to CharLookupDialog**

After the definitions, add a row of candidate word chips:

```kotlin
if (info.wordCandidates.isNotEmpty()) {
    Spacer(Modifier.height(12.dp))
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
        info.wordCandidates.forEach { word ->
            Surface(
                onClick = { /* select each char in word */ },
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
```

- [ ] **Step 7: Add onSelectionChanged to detect text selection**

`SelectionContainer` doesn't support `onSelectionChanged` directly. Use `BasicText` or a `Modifier.onSelectionChanged` approach. The cleanest Compose approach is to add a `pointerInput` modifier with `detectTapGestures(onLongPress = ...)` on the Text, then use `SelectionContainer` to enable text selection. For the long-press detection, we'll need to get the character at the tap offset.

Simpler approach: use `ClickableText` with `AnnotatedString` + `pressIndicator` or just track selection via a state wrapper.

Actually, the simplest Compose approach is: use `BasicTextField` in read-only mode with `readOnly = true` inside a `SelectionContainer`, and add `onValueChange` to detect the selected text. But `BasicTextField` has visual differences.

Better approach: keep `SelectionContainer` + add `Modifier.pointerInput` for long press, find char at offset using `TextLayoutResult`. This gives us the tapped character directly without system selection handles if we want, but we agreed to use SelectionContainer.

The pragmatic approach: use `SelectionContainer` for visual selection, and use `Modifier.onGloballyPositioned` to enable selection. Since `SelectionContainer` already allows the user to long-press and see selection handles, we listen for `onSelectionChanged` via a custom wrapper.

Actually, the most straightforward and working approach in Compose is: wrap with `SelectionContainer`, but detect the selection end event via a `DisposableEffect` that monitors the system clipboard or selection state. This is over-engineered.

**Simplest working approach**: Use `BasicTextField(readOnly = true)` which natively supports `onTextLayout` and `onValueChange` (empty for read-only), but more importantly, Compose `TextField` with `readOnly = true` inside a `SelectionContainer` allows the user to select text. We can use `Modifier.onSelectionChanged` from compose-foundation.

Final decision: wrap with `SelectionContainer`, and use `Modifier.pointerInput` with `detectTapGestures(onLongPress = { offset -> ... })` to find the character at the tap position using the `TextLayoutResult`. This gives us the character immediately on long press, while the system selection handles still appear for the user to adjust.

Add a `textLayoutResult` state variable:

```kotlin
var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

// In Text:
onTextLayout = { textLayoutResult = it }
modifier = Modifier.pointerInput(currentText) {
    detectTapGestures(
        onLongPress = { offset ->
            val result = textLayoutResult ?: return@detectTapGestures
            val charOffset = result.getOffsetForPosition(offset)
            if (charOffset in currentText.indices) {
                viewModel.onTextSelected(currentText[charOffset].toString())
            }
        }
    )
}
```

Add these imports:
```kotlin
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.TextLayoutResult
```

- [ ] **Step 8: Build and verify**

Run: `.\gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

---

### Task 3: Final cleanup and verification

**Files:**
- Modify: `app/src/main/java/com/beisong/app/ui/reader/ReaderScreen.kt`
- Modify: `app/src/main/java/com/beisong/app/ui/reader/ReaderViewModel.kt`

- [ ] **Step 1: Remove unused LookupMode strings from strings.xml if present**

- [ ] **Step 2: Full build**

Run: `.\gradlew.bat assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "feat: long-press selection lookup replaces tap-to-lookup mode"
```
