# Long-press Character Lookup

## Summary
Replace the current tap-to-lookup mode (查字模式) with a long-press selection gesture on reading text. User long-presses text → system selection appears → first selected char's dictionary entry is shown in a popup, along with nearby word candidate suggestions.

## Changes

### 1. Reading Text — SelectionContainer
- Wrap the reading `Text` composable with `SelectionContainer`
- On selection change, extract the first Chinese character from selected text
- Submit to ViewModel for dictionary lookup

### 2. Word Candidate Logic
- When a character is selected, look backward 1-2 chars and forward 1-2 chars in the current segment text
- Generate candidate 2-char and 3-char word combinations (e.g., select "观" → "以观", "观其", "以观其")
- Display candidate words in the popup; tapping a candidate word shows each character's dictionary entry in sequence (or a combined note)

### 3. Popup — Reduced Font Sizes
- Title character: 24sp (was 36sp)
- Pinyin heading: 16sp (was 20sp)
- Definition text: 14sp (was 16sp)
- Detail citations: 12sp (was 13sp)

### 4. Remove Lookup Mode
- Delete `isLookupMode`, `toggleLookupMode()` from ViewModel/UIState
- Delete `LookupGrid` composable
- Bottom bar: remove "查字/阅读" toggle button
- Bottom bar: show only prev/next navigation and segment counter

### 5. File Changes
- `ReaderViewModel.kt` — remove lookup mode state, add `selectCharOnTextSelection(text: String)` that extracts first Chinese char and calls lookup
- `ReaderScreen.kt` — wrap Text in SelectionContainer, add onSelectionChanged callback, remove LookupGrid, shrink popup fonts, remove lookup toggle from bottom bar
- `CharDictionary.kt` — no changes needed (data model already supports the new format)

## Data Flow
1. User long-press selects "观其妙" → `onSelectionChanged` fires with selected text
2. ViewModel extracts first CJK char "观" → `charDict.lookup("观")`
3. ViewModel also computes word candidates ("以观", "观其", "以观其") from segment text
4. Popup renders: title "观", reading sections, word candidate chips
5. Tapping a word candidate → highlights it / shows each char in sequence
