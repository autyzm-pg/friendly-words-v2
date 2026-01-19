package com.example.friendly_words.child_app.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.friendly_words.R
import com.example.friendly_words.child_app.theme.LightBlue
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Math.sqrt

@AndroidEntryPoint
class MainActivityChild : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        setContent {
            val viewModel: ChildMainViewModel = hiltViewModel()
            MaterialTheme {
                ScreenNavigationGame(viewModel)
            }
        }
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

@Composable
fun calculateResponsiveDp(referenceDp: Dp, referenceWidth: Float = 2560f, referenceHeight: Float = 1600f): Dp {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp * configuration.densityDpi / 160f
    val screenHeight = configuration.screenHeightDp * configuration.densityDpi / 160f
    val widthRatio = screenWidth / referenceWidth
    val heightRatio = screenHeight / referenceHeight
    val scalingFactor = sqrt((widthRatio * heightRatio).toDouble()).toFloat()
    return (referenceDp.value * scalingFactor).dp
}

@Composable
fun InformationScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LightBlue)
    ) {
        Spacer(modifier = Modifier.height(calculateResponsiveDp(55.dp)))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(calculateResponsiveDp(16.dp)))
            Text(
                text = "Przyjazne Słowa",
                color = Color.White,
                fontSize = calculateResponsiveFontSize(55.sp),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(calculateResponsiveDp(60.dp)))

            Text(
                text = "Rozpoznawanie słów na podstawie zdjęć",
                color = Color.White,
                fontSize = calculateResponsiveFontSize(35.sp),
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.9f),
            color = Color.White,
            shape = RoundedCornerShape(topStart = 100.dp, topEnd = 100.dp),
        ) {
            Column(
                modifier = Modifier.padding(calculateResponsiveDp(16.dp)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Aplikacje powstają w ramach wspólnego projektu \"non-profit\" i \"open-source\" ",
                    fontSize = calculateResponsiveFontSize(27.sp),
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(calculateResponsiveDp(16.dp)))
                Text(
                    text = "Politechniki Gdańskiej i fundacji Instytut Wspomagania Rozwoju Dziecka w Gdańsku (http://iwrd.pl/).",
                    fontSize = calculateResponsiveFontSize(26.sp),
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(calculateResponsiveDp(16.dp)))
                Text(
                    text = "IWRD, Politechnika Gdańska, ani żaden uczestnik projektu nie odnosi korzyści materialnych z udziału w wytwarzaniu aplikacji.",                    fontSize = calculateResponsiveFontSize(26.sp),
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = calculateResponsiveFontSize(42.sp)
                )
                Spacer(modifier = Modifier.height(calculateResponsiveDp(16.dp)))


                Text(
                    text = "Więcej informacji na stronie: https://tiapps.org/",
                    fontSize = calculateResponsiveFontSize(26.sp),
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(calculateResponsiveDp(20.dp)))
                Text(
                    text = "Do poprawnego działania potrzebne jest para aplikacji: Przyjazne Słowa i Przyjazne Słowa Ustawienia!",
                    fontSize = calculateResponsiveFontSize(26.sp),
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(calculateResponsiveDp(16.dp)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(calculateResponsiveDp(85.dp)))
                    Image(
                        painter = painterResource(id = R.drawable.pg_logo),
                        contentDescription = "Politechnika Gdańska Logo",
                        modifier = Modifier.size(calculateResponsiveDp(350.dp))
                    )
                    Spacer(modifier = Modifier.width(calculateResponsiveDp(95.dp)))
                    Image(
                        painter = painterResource(id = R.drawable.iwrd_logo),
                        contentDescription = "Instytut Wspomagania Rozwoju Dziecka Logo",
                        modifier = Modifier.size(calculateResponsiveDp(420.dp))
                    )
                    Spacer(modifier = Modifier.width(calculateResponsiveDp(70.dp)))
                    Image(
                        painter = painterResource(id = R.drawable.eti_logo),
                        contentDescription = "ETI Logo",
                        modifier = Modifier.size(calculateResponsiveDp(200.dp))
                    )
                    Spacer(modifier = Modifier.width(calculateResponsiveDp(80.dp)))
                }
            }
        }
    }
}

