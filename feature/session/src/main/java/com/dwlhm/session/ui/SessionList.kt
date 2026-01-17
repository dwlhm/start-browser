package com.dwlhm.session.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dwlhm.browser.session.SessionDescriptor
import kotlinx.coroutines.flow.Flow

@Composable
fun SessionList(
    sessions: Flow<List<SessionDescriptor>>,
    modifier: Modifier = Modifier,
    onSessionClick: (SessionDescriptor) -> Unit = {},
    onSessionClose: (SessionDescriptor) -> Unit = {},
) {
    val sessionList by sessions.collectAsState(initial = emptyList())

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (sessionList.isEmpty()) {
            EmptySessionState()
        } else {
            sessionList.forEach { session ->
                SessionItem(
                    session = session,
                    onClick = { onSessionClick(session) },
                    onClose = { onSessionClose(session) }
                )
            }
        }
    }
}

@Composable
private fun SessionItem(
    session: SessionDescriptor,
    onClick: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF5F5F5))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Favicon placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = session.title.firstOrNull()?.uppercase()
                    ?: session.url.firstOrNull()?.uppercase()
                    ?: "?",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666)
                )
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            BasicText(
                text = session.title,
                style = TextStyle(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            BasicText(
                text = session.url,
                style = TextStyle(
                    fontSize = 12.sp,
                    color = Color(0xFF888888)
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Close button
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8E8E8))
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            BasicText(
                text = "×",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF666666)
                )
            )
        }
    }
}

@Composable
private fun EmptySessionState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        BasicText(
            text = "No sessions yet",
            style = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFFAAAAAA)
            )
        )
    }
}