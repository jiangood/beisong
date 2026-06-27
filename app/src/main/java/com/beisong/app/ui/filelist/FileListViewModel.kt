package com.beisong.app.ui.filelist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beisong.app.data.FileRepository
import com.beisong.app.data.ReadingHistory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FileInfo(
    val fileName: String,
    val displayName: String,
    val lastOpenedAt: Long = 0
)

data class FileListUiState(
    val files: List<FileInfo> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class FileListViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FileRepository(application)
    private val history = ReadingHistory(application)

    private val _uiState = MutableStateFlow(FileListUiState())
    val uiState: StateFlow<FileListUiState> = _uiState.asStateFlow()

    init {
        loadFiles()
    }

    private fun loadFiles() {
        viewModelScope.launch {
            try {
                val files = withContext(Dispatchers.IO) {
                    repository.listTextFiles().map { fileName ->
                        FileInfo(
                            fileName = fileName,
                            displayName = repository.displayName(fileName),
                            lastOpenedAt = history.getLastOpened(fileName)
                        )
                    }.sortedByDescending { it.lastOpenedAt }
                }
                _uiState.value = FileListUiState(files = files, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = FileListUiState(isLoading = false, error = e.message)
            }
        }
    }

    fun refresh() {
        loadFiles()
    }
}
