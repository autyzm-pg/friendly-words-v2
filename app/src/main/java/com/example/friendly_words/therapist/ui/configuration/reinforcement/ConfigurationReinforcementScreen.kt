package com.example.friendly_words.therapist.ui.configuration.reinforcement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.friendly_words.therapist.ui.theme.DarkBlue
import com.example.shared.data.another.ConfigurationReinforcementState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.composables.core.ScrollArea
import com.composables.core.VerticalScrollbar
import com.composables.core.Thumb
import com.composables.core.rememberScrollAreaState


@Composable
fun ConfigurationReinforcementScreen(

    state: ConfigurationReinforcementState,
    onEvent: (ConfigurationReinforcementEvent) -> Unit,
    onBackClick: () -> Unit
) {
    val leftScrollState = rememberScrollState()
    val leftScrollAreaState = rememberScrollAreaState(leftScrollState)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // lewa strona ekranu
            val configuration = LocalConfiguration.current
            val screenHeight = configuration.screenHeightDp.dp

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.TopCenter
            ) {
                ScrollArea(state = leftScrollAreaState) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(leftScrollState),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Wzmocnienia słowne",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkBlue,
                                modifier = Modifier.padding(bottom = screenHeight * 0.03f)
                            )

                            val praiseWords = state.praiseStates.keys.toList().chunked(3)

                            praiseWords.forEach { row ->
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = screenHeight * 0.05f)
                                ) {
                                    row.forEach { word ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = state.praiseStates[word] == true,
                                                onCheckedChange = {
                                                    onEvent(
                                                        ConfigurationReinforcementEvent.TogglePraise(
                                                            word,
                                                            it
                                                        )
                                                    )
                                                },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = DarkBlue,
                                                    uncheckedColor = Color.Gray,
                                                    checkmarkColor = Color.White
                                                )
                                            )
                                            Text(
                                                text = word,
                                                fontSize = 24.sp,
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(screenHeight * 0.08f))

                            Column(
                                modifier = Modifier
                                    .widthIn(max = 450.dp)
                                    .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Informacja",
                                    tint = DarkBlue,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .padding(bottom = 8.dp)
                                )

                                Text(
                                    text = "Czytanie pochwał słownych po poprawnej odpowiedzi jest zawsze włączone. ",
                                    fontSize = 16.sp,
                                    color = Color.Black,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        VerticalScrollbar(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .fillMaxHeight()
                                .width(4.dp)
                        ) {
                            Thumb(Modifier.background(Color.Gray))
                        }
                    }
                }
            }

            //prawa strona ekranu
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Wzmocnienia wizualne",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkBlue,
                    modifier = Modifier.padding(bottom = screenHeight * 0.08f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Animacje",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Switch(
                        checked = state.animationsEnabled,
                        onCheckedChange = {
                            onEvent(ConfigurationReinforcementEvent.ToggleAnimations(it))
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = DarkBlue,
                            checkedTrackColor = DarkBlue.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }
            }
        }
    }
}
