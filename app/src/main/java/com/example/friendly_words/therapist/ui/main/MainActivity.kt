package com.example.friendly_words.therapist.ui.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.lang.Math.sqrt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        setContent {
            MaterialTheme {
                ScreenNavigation()
            }
        }
    }
}

@Composable
fun ScreenNavigation() {
    var showFirstScreen by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(5000L)
        showFirstScreen = false
    }

    if (showFirstScreen) {
        com.example.friendly_words.child_app.main.InformationScreen()
    } else {
        MainScreen()
    }
}

@Composable
fun calculateResponsiveFontSize(referenceFontSize: TextUnit): TextUnit {
    val referenceWidth = 2560f
    val referenceHeight = 1600f
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp * configuration.densityDpi / 160f
    val screenHeight = configuration.screenHeightDp * configuration.densityDpi / 160f
    val widthRatio = screenWidth / referenceWidth
    val heightRatio = screenHeight / referenceHeight
    val scalingFactor = sqrt((widthRatio * heightRatio).toDouble()).toFloat()
    return (referenceFontSize.value * scalingFactor).sp
}