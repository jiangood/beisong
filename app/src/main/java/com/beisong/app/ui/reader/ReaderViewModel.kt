package com.beisong.app.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beisong.app.data.CharDictionary
import com.beisong.app.data.CharInfo
import com.beisong.app.data.FileRepository
import com.beisong.app.data.ReadingHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application)
    private val charDict = CharDictionary(application)
    private val history = ReadingHistory(application)

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            charDict.load()
        }
    }

    fun loadFile(fileName: String) {
        viewModelScope.launch {
            try {
                val segments = withContext(Dispatchers.IO) {
                    repository.loadSegments(fileName)
                }
                val lastIndex = history.getLastSegment(fileName).coerceIn(0, segments.size - 1)
                history.recordOpen(fileName)
                _uiState.value = ReaderUiState(
                    fileName = repository.displayName(fileName),
                    rawFileName = fileName,
                    segments = segments,
                    currentIndex = lastIndex,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = ReaderUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun nextSegment() {
        _uiState.update { state ->
            if (state.currentIndex < state.segments.size - 1) {
                val newIndex = state.currentIndex + 1
                history.recordProgress(state.rawFileName, newIndex)
                state.copy(currentIndex = newIndex, selectedChar = null, wordCandidates = emptyList())
            } else state
        }
    }

    fun prevSegment() {
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                val newIndex = state.currentIndex - 1
                history.recordProgress(state.rawFileName, newIndex)
                state.copy(currentIndex = newIndex, selectedChar = null, wordCandidates = emptyList())
            } else state
        }
    }

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

    fun clearSelection() {
        _uiState.update { it.copy(selectedChar = null, wordCandidates = emptyList()) }
    }

    fun onWordCandidateClicked(word: String) {
        val chars = word.filter { it in '\u4e00'..'\u9fff' || it in '\u3400'..'\u4dbf' }
        if (chars.isNotEmpty()) {
            val firstChar = chars.first().toString()
            val found = charDict.lookup(firstChar)
            _uiState.update {
                it.copy(
                    selectedChar = found ?: CharInfo(firstChar, emptyList())
                )
            }
        }
    }
}
