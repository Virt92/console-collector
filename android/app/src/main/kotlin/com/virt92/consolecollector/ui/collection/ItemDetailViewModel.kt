package com.virt92.consolecollector.ui.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virt92.consolecollector.data.model.CollectionItemDto
import com.virt92.consolecollector.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ItemDetailUiState(
    val loading: Boolean = false,
    val item: CollectionItemDto? = null,
    val error: String? = null,
)

class ItemDetailViewModel(
    private val container: AppContainer,
    private val itemId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(ItemDetailUiState())
    val state: StateFlow<ItemDetailUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            runCatching {
                val list = container.repository.listCollection()
                list.firstOrNull { it.id == itemId }
                    ?: error("Item not found")
            }
                .onSuccess { item -> _state.update { ItemDetailUiState(item = item) } }
                .onFailure { e -> _state.update { it.copy(loading = false, error = e.message) } }
        }
    }

    fun delete(onDone: () -> Unit) {
        viewModelScope.launch {
            runCatching { container.repository.deleteCollectionItem(itemId) }
                .onSuccess { onDone() }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun share(onShared: (String) -> Unit) {
        viewModelScope.launch {
            runCatching { container.repository.shareItem(itemId) }
                .onSuccess { onShared(it.url) }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }
}
