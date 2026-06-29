package com.beisong.app.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
    val error: String? = null
)

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application)
    private val history = ReadingHistory(application)

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

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
                state.copy(currentIndex = newIndex)
            } else state
        }
    }

    fun prevSegment() {
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                val newIndex = state.currentIndex - 1
                history.recordProgress(state.rawFileName, newIndex)
                state.copy(currentIndex = newIndex)
            } else state
        }
    }
}
