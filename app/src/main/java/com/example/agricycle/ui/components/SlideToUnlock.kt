package com.example.agricycle.ui.components

import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale // Added import
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource // Added import
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.agricycle.R // Added import for R class
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SlideToUnlock(
    onUnlocked: () -> Unit,
    modifier: Modifier = Modifier,
    text: String = "Slide to Start Farming",
    resetAfterUnlock: Boolean = true
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val width = 300.dp
    val thumbWidth = 64.dp

    val widthPx = with(density) { width.toPx() }
    val thumbWidthPx = with(density) { thumbWidth.toPx() }
    val dragRange = widthPx - thumbWidthPx
    val unlockThreshold = dragRange * 0.8f // Unlock when 80% dragged

    val offsetX = remember { Animatable(0f) }
    var isUnlocked by remember { mutableStateOf(false) }

    // Custom colors for agricultural theme
    val fieldGreen = Color(0xFF4CAF50)
    // val earthBrown = Color(0xFF795548) // Not used currently

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            val currentOffset = offsetX.value
            val newOffset = (currentOffset + delta).coerceIn(0f, dragRange)
            offsetX.snapTo(newOffset)
        }
    }

    LaunchedEffect(isUnlocked) {
        if (isUnlocked) {
            // Trigger haptic feedback
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java) as Vibrator?
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))

            // Show toast message
            // Toast.makeText(context, "Unlocked successfully!", Toast.LENGTH_SHORT).show()

            // Call the callback
            onUnlocked()

            // Reset the slider if needed
            if (resetAfterUnlock) {
                kotlinx.coroutines.delay(200) // Small delay before resetting
                offsetX.animateTo(0f, animationSpec = tween(300))
                isUnlocked = false // Reset unlock state
            }
        }
    }

    Box(
        modifier = modifier
            .width(width)
            .height(thumbWidth) // Match thumb height
            .background(
                color = fieldGreen,
                shape = RoundedCornerShape(32.dp)
            )
            .clip(RoundedCornerShape(32.dp)), // Clip the content to the rounded shape
        contentAlignment = Alignment.CenterStart
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.back),
            contentDescription = null, // Decorative image
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Or FillBounds, depending on desired scaling
        )

        // Background text
        Text(
            text = text,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = thumbWidth / 2) // Avoid overlap with thumb start/end
                .alpha(0.7f),
            textAlign = TextAlign.Center,
            color = fieldGreen,
            style = MaterialTheme.typography.bodyLarge
        )

        // Sliding thumb with tractor icon
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .size(thumbWidth)
                .background(fieldGreen, RoundedCornerShape(28.dp))
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetX.value >= unlockThreshold) {
                                // Animate to end and trigger unlock
                                offsetX.animateTo(dragRange, animationSpec = tween(100))
                                isUnlocked = true
                            } else {
                                // Animate back to start
                                offsetX.animateTo(0f, animationSpec = tween(300))
                            }
                        }
                    }
                )
                .padding(4.dp), // Padding inside the thumb background
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Agriculture,
                contentDescription = "Slide to unlock",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
