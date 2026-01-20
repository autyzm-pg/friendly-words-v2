package com.example.friendly_words.child_app.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.friendly_words.child_app.theme.Blue
import com.example.friendly_words.child_app.theme.LightBlue
import kotlin.math.min
import kotlin.math.max

@Composable
fun MainScreen(
    configurationDao: com.example.shared.data.daos.ConfigurationDao,
    onPlayClick: () -> Unit,
    canPlay: Boolean
) {
    val activeConfig by configurationDao.getActiveConfiguration().collectAsState(initial = null)
    val isTestMode = activeConfig?.activeMode == "test"

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue)
    ) {
        fun clamp(v: Float, minV: Float, maxV: Float) = max(minV, min(maxV, v))

        val baseW = maxWidth.value
        val baseH = maxHeight.value

        val titleSp    = clamp(baseW * 0.055f, 48f, 90f).sp
        val subtitleSp = clamp(baseW * 0.020f, 22f, 40f).sp
        val warningSp  = clamp(baseW * 0.022f, 24f, 44f).sp
        val playSize = clamp(baseW * 0.35f, 420f, 700f).dp

        val contentWidth = clamp(baseW * 0.92f, 520f, 1200f).dp
        val spacing = clamp(baseH * 0.05f, 20f, 48f).dp

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .width(contentWidth),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing)
            ) {
                Text(
                    text = "Przyjazne Słowa",
                    fontSize = titleSp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Aktywny krok uczenia: ${activeConfig?.name ?: "brak"} (tryb: ${if (isTestMode) "test" else "uczenie"})",
                    fontSize = subtitleSp,
                    color = LightBlue
                )

                com.example.friendly_words.child_app.components.PlayButton(
                    onClick = onPlayClick,
                    enabled = canPlay,
                    size = playSize
                )

                if (!canPlay) {
                    Text(
                        text = "Brak materiałów w kroku uczenia. Dodaj materiały lub zmień krok uczenia w aplikacji terapeuty.",
                        fontSize = warningSp,
                        color = Color.White
                    )
                }
            }
        }
    }
}
