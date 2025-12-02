package com.example.friendly_words.therapist.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.friendly_words.therapist.ui.theme.DarkBlue

@Composable
fun NumberSelectorForPicturesPlain(
    label: String,
    minValue: Int,
    maxValue: Int,
    value: Int,
    onValueChange: (Int) -> Unit = {},
    enabled: Boolean = true,
    labelColor: Color = Color.Black
) {
    val canDecrement = value > minValue
    val canIncrement = value < maxValue

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            textAlign = TextAlign.Center
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (!enabled) return@Button
                    if (canDecrement) onValueChange(value - 1)
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
                fontWeight = FontWeight.Medium,
                color = labelColor
            )

            Spacer(Modifier.width(24.dp))

            Button(
                onClick = {
                    if (!enabled) return@Button
                    if (canIncrement) onValueChange(value + 1)
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = if (canIncrement && enabled) DarkBlue else Color.LightGray
                ),
                enabled = enabled
            ) { Text("+", fontSize = 24.sp, color = Color.White) }
        }
    }
}
