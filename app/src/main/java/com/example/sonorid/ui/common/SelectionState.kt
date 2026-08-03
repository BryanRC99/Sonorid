// app/src/main/java/com/example/sonorid/ui/common/SelectionState.kt
package com.example.sonorid.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class SelectionState<T> {
    var selectedIds by mutableStateOf<Set<T>>(emptySet())
        private set

    val isActive: Boolean get() = selectedIds.isNotEmpty()
    val count: Int get() = selectedIds.size

    fun isSelected(id: T): Boolean = id in selectedIds

    fun toggle(id: T) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun selectAll(ids: List<T>) {
        selectedIds = ids.toSet()
    }

    fun clear() {
        selectedIds = emptySet()
    }
}

@Composable
fun <T> rememberSelectionState(): SelectionState<T> = remember { SelectionState() }