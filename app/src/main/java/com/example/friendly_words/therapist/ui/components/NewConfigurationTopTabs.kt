package com.example.friendly_words.therapist.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun NewConfigurationTopTabs(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    tabTitles: List<String> = listOf("MATERIAŁ", "UCZENIE", "WZMOCNIENIA", "TEST DLA UCZNIA", "PODSUMOWANIE")
) {
    Column {
        Row(modifier = Modifier.height(50.dp)) {
            tabTitles.forEachIndexed { index, title ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (index == selectedTabIndex) Color(0xFFADD8E6)
                            else Color.White
                        )
                        .clickable { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        color = if (index == selectedTabIndex) Color.Black else Color.DarkGray
                    )
                }

                if (index < tabTitles.lastIndex) {
                    Divider(
                        color = Color.LightGray,
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                    )
                }
            }
        }
        Divider(
            color = Color.LightGray,
            thickness = 1.dp
        )
    }
}

@Preview(showBackground = true, widthDp = 1000, heightDp = 100)
@Composable
fun PreviewNewConfigurationTopTabs() {
    NewConfigurationTopTabs(
        selectedTabIndex = 2,
        onTabSelected = {}
    )
}
