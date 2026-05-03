package com.virt92.consolecollector.ui.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.virt92.consolecollector.data.model.GameItemDto
import com.virt92.consolecollector.data.model.GameStats
import com.virt92.consolecollector.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GamesUiState(
    val items: List<GameItemDto> = emptyList(),
    val stats: GameStats? = null,
    val loading: Boolean = false,
    val error: String? = null,
)

class GamesViewModel(private val container: AppContainer) : ViewModel() {
    private val _state = MutableStateFlow(GamesUiState())
    val state: StateFlow<GamesUiState> = _state.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                val items = container.repository.listGameItems()
                val stats = runCatching { container.repository.gameStats() }.getOrNull()
                items to stats
            }
                .onSuccess { (items, stats) ->
                    _state.update {
                        it.copy(loading = false, items = items, stats = stats)
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(loading = false, error = e.message) }
                }
        }
    }
}
