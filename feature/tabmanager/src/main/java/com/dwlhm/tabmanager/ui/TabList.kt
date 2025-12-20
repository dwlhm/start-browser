package com.dwlhm.tabmanager.ui

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dwlhm.tabmanager.api.TabId
import com.dwlhm.tabmanager.api.TabSnapshot

@Composable
fun TabList(
    tabs: List<TabSnapshot>,
    activeTabId: TabId?,
    onSelect: (TabId) -> Unit,
    onClose: (TabId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn (
        modifier = modifier
            .padding(horizontal = 8.dp),
    ) {
        items(
            items = tabs,
            key = { it.id.toString() }
        ) { session ->
            TabItem(
                title = session.title,
                isActive = session.id == activeTabId,
                onClick = { onSelect(session.id) },
                onClose = { onClose(session.id) },
                modifier = Modifier.padding(end = 6.dp)
            )
        }
    }
}
