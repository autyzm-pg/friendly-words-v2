package com.example.friendly_words.therapist.ui.components

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import com.example.friendly_words.therapist.ui.theme.DarkBlue

@Composable
fun NewConfigurationTopBar(title: String, onBackClick: () -> Unit) {
    Box(modifier = Modifier.background(DarkBlue)) {
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
            backgroundColor = Color.Transparent,
            elevation = 0.dp,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 30.sp,
                        modifier = Modifier.weight(1f),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                }
            }
        )
    }
}
