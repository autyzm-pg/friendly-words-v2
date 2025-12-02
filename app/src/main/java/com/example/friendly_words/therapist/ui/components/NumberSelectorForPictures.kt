package com.example.friendly_words.therapist.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.friendly_words.therapist.ui.theme.DarkBlue

@Composable
fun NumberSelectorForPictures(
    label: String,
    minValue: Int,
    maxValue: Int,
    value: Int,
    onValueChange: (Int) -> Unit = {},
    enabled: Boolean = true,
    labelEnabled: Boolean = enabled,
    labelColor: Color = Color.Black,
    onDisabledDecrementClick: (() -> Unit)? = null,
    onDisabledIncrementClick: (() -> Unit)? = null
) {
    var showPopup by remember { mutableStateOf(false) }
    var popupMessage by remember { mutableStateOf("") }

    val canDecrement = value > minValue
    val canIncrement = value < maxValue

    val useInternalDecrementPopup = onDisabledDecrementClick == null
    val useInternalIncrementPopup = onDisabledIncrementClick == null

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = if (labelEnabled) labelColor else labelColor.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (!enabled) return@Button
                    if (canDecrement) {
                        onValueChange(value - 1)
                    } else {
                        if (useInternalDecrementPopup) {
                            popupMessage = "Nie możesz zmniejszyć wartości poniżej $minValue."
                            showPopup = true
                        }
                        onDisabledDecrementClick?.invoke()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (canDecrement && enabled) DarkBlue else Color.LightGray
                ),
                enabled = enabled
            ) { Text("-", fontSize = 24.sp, color = Color.White) }

            Spacer(Modifier.width(24.dp))

            Text(
                text = value.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (labelEnabled) labelColor else labelColor.copy(alpha = 0.6f)
            )

            Spacer(Modifier.width(24.dp))

            Button(
                onClick = {
                    if (!enabled) return@Button
                    if (canIncrement) {
                        onValueChange(value + 1)
                    } else {
                        if (useInternalIncrementPopup) {
                            popupMessage = "Nie możesz zwiększyć wartości powyżej $maxValue."
                            showPopup = true
                        }
                        onDisabledIncrementClick?.invoke()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (canIncrement && enabled) DarkBlue else Color.LightGray
                ),
                enabled = enabled
            ) { Text("+", fontSize = 24.sp, color = Color.White) }
        }
    }

    if (showPopup) {
        AlertDialog(
            onDismissRequest = { showPopup = false },
            title = { Text("Informacja") },
            text = { Text(popupMessage) },
            confirmButton = {
                TextButton(onClick = { showPopup = false }) {
                    Text("OK", color = DarkBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
