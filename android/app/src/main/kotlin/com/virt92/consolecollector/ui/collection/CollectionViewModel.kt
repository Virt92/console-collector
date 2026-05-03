package com.virt92.consolecollector.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virt92.consolecollector.data.model.CollectionItemDto
import com.virt92.consolecollector.data.model.CollectionStats
import com.virt92.consolecollector.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CollectionUiState(
    val loading: Boolean = false,
    val items: List<CollectionItemDto> = emptyList(),
    val stats: CollectionStats? = null,
    val error: String? = null,
)

class CollectionViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(CollectionUiState())
    val state: StateFlow<CollectionUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val items = container.repository.listCollection()
                val stats = runCatching { container.repository.stats() }.getOrNull()
                items to stats
            }
                .onSuccess { (items, stats) ->
                    _state.update { it.copy(loading = false, items = items, stats = stats) }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message) }
                }
        }
    }
}
