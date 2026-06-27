package com.beisong.app.ui.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beisong.app.data.FileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReaderUiState(
    val fileName: String = "",
    val segments: List<String> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ReaderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application)

    private val _uiState = MutableStateFlow(ReaderUiState())
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    fun loadFile(fileName: String) {
        viewModelScope.launch {
            try {
                val segments = withContext(Dispatchers.IO) {
                    repository.loadSegments(fileName)
                }
                _uiState.value = ReaderUiState(
                    fileName = repository.displayName(fileName),
                    segments = segments,
                    currentIndex = 0,
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
                state.copy(currentIndex = state.currentIndex + 1)
            } else state
        }
    }

    fun prevSegment() {
        _uiState.update { state ->
            if (state.currentIndex > 0) {
                state.copy(currentIndex = state.currentIndex - 1)
            } else state
        }
    }
}
