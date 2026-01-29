package com.dwlhm.ui.scaffold

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DraggableScaffold(
    sheetContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
    peekHeightPx: Float = 0.1f
) {
    val scope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val fullHeightPx = with(density) { maxHeight.toPx() }

        val closedOffset = fullHeightPx - peekHeightPx
        val openedOffset = 0f

        val offsetAnimatable = remember { Animatable(closedOffset) }

        Box(modifier = Modifier.fillMaxSize()) {
            content()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
//                    .fillMaxHeight()
                    // Menggunakan lambda offset agar performa tetap 60fps
                    .offset { IntOffset(0, offsetAnimatable.value.roundToInt()) }
//                    .background(Color.White)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    // 4. Logika Snap yang lebih presisi
                                    val targetValue = if (offsetAnimatable.value < closedOffset / 2) {
                                        openedOffset
                                    } else {
                                        closedOffset
                                    }

                                    offsetAnimatable.animateTo(
                                        targetValue = targetValue,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessLow
                                        )
                                    )
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    val nextValue = offsetAnimatable.value + dragAmount
                                    // 5. Batasi agar tidak ditarik melebihi batas bawah (closedOffset)
                                    offsetAnimatable.snapTo(nextValue.coerceIn(openedOffset, closedOffset))
                                }
                            }
                        )
                    }
            ) {
                sheetContent()
            }
        }
    }
}