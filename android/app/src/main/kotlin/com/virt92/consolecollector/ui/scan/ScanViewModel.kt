package com.virt92.consolecollector.ui.scan

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virt92.consolecollector.data.model.CreateCollectionItemRequest
import com.virt92.consolecollector.data.model.RecognizeResponse
import com.virt92.consolecollector.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ScanUiState(
    val photoBytes: List<ByteArray> = emptyList(),
    val recognizing: Boolean = false,
    val recognized: RecognizeResponse? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
)

class ScanViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(ScanUiState())
    val state: StateFlow<ScanUiState> = _state.asStateFlow()

    fun reset() {
        _state.value = ScanUiState()
    }

    fun addPhoto(bytes: ByteArray) {
        if (_state.value.photoBytes.size >= 3) return
        _state.update { it.copy(photoBytes = it.photoBytes + bytes) }
    }

    fun recognize() {
        val photos = _state.value.photoBytes
        if (photos.isEmpty()) {
            _state.update { it.copy(error = "Take at least one photo") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(recognizing = true, error = null) }
            val dataUris = photos.map { bytesToDataUri(it) }
            runCatching { container.repository.recognize(dataUris) }
                .onSuccess { res -> _state.update { it.copy(recognizing = false, recognized = res) } }
                .onFailure { e -> _state.update { it.copy(recognizing = false, error = e.message) } }
        }
    }

    fun confirmAndAdd(onDone: () -> Unit) {
        val recognized = _state.value.recognized ?: return
        val consoleModelId = recognized.consoleModelId
        if (consoleModelId == null) {
            _state.update {
                it.copy(error = "We couldn't match this to a known console — try clearer photos or pick from the catalog manually.")
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            runCatching {
                container.repository.addToCollection(
                    CreateCollectionItemRequest(
                        consoleModelId = consoleModelId,
                        recognized = recognized.details,
                    ),
                )
            }
                .onSuccess {
                    _state.update { it.copy(saving = false, saved = true) }
                    onDone()
                }
                .onFailure { e -> _state.update { it.copy(saving = false, error = e.message) } }
        }
    }

    private fun bytesToDataUri(bytes: ByteArray): String {
        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
        return "data:image/jpeg;base64,$b64"
    }
}
