package com.example.agricycle.ui.utils

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun FadeInAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500)),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun ScaleInAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(0.8f) }
    
    LaunchedEffect(Unit) {
        scale = 1f
    }
    
    Box(
        modifier = modifier.graphicsLayer(
            scaleX = scale,
            scaleY = scale
        )
    ) {
        content()
    }
}

@Composable
fun SlideInAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var offset by remember { mutableStateOf(100f) }
    
    LaunchedEffect(Unit) {
        offset = 0f
    }
    
    Box(
        modifier = modifier.graphicsLayer(
            translationY = offset
        )
    ) {
        content()
    }
}

@Composable
fun CombinedAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    var scale by remember { mutableStateOf(0.8f) }
    var offset by remember { mutableStateOf(100f) }
    
    LaunchedEffect(Unit) {
        visible = true
        scale = 1f
        offset = 0f
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(500)) + 
                scaleIn(animationSpec = tween(500)) +
                slideInVertically(animationSpec = tween(500)),
        exit = fadeOut(animationSpec = tween(500)) +
               scaleOut(animationSpec = tween(500)) +
               slideOutVertically(animationSpec = tween(500)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationY = offset
                )
        ) {
            content()
        }
    }
} 