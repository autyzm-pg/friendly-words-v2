package com.example.friendly_words.therapist.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.friendly_words.therapist.ui.configuration.settings.ConfigurationSettingsScreen
import com.example.friendly_words.therapist.ui.configuration.list.ConfigurationsListScreen
import com.example.friendly_words.therapist.ui.materials.creating_new.MaterialsCreatingNewMaterialScreen
import com.example.friendly_words.therapist.ui.materials.list.MaterialsListScreen
import com.example.friendly_words.therapist.ui.theme.DarkBlue
import com.example.friendly_words.therapist.ui.theme.LightBlue2

object NavRoutes {
    const val MAIN = "main"

    const val MATERIALS = "materials"
    const val MATERIAL_CREATE = "materials/create"
    const val MATERIAL_EDIT = "materials/edit/{resourceId}"

    const val CONFIG_LIST = "config"
    const val CONFIG_CREATE = "config/create"
    const val CONFIG_EDIT = "config/edit/{configId}"
}


@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val viewModel: MainScreenViewModel = hiltViewModel()
    val activeConfig by viewModel.activeConfiguration.collectAsState()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.MAIN
    ) {

        composable(NavRoutes.MAIN) {
            MainContent(
                onConfigClick = { navController.navigate(NavRoutes.CONFIG_LIST) },
                onMaterialsClick = { navController.navigate(NavRoutes.MATERIALS) },
                activeConfiguration = activeConfig?.let {
                    it.name to (it.activeMode ?: "uczenie")
                }
            )
        }

        composable(NavRoutes.MATERIALS) {
            MaterialsListScreen(navController = navController,
                onBackClick = { navController.popBackStack() },
                onCreateClick = { navController.navigate(NavRoutes.MATERIAL_CREATE) },
                onEditClick = { resourceId ->
                    navController.navigate("materials/edit/$resourceId")
                }
            )
        }


        composable(NavRoutes.MATERIAL_CREATE) {
            MaterialsCreatingNewMaterialScreen(navController = navController,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { savedResourceId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("newlySavedResourceId", savedResourceId)
                    navController.popBackStack()
                }

            )
        }

        composable(
            route = NavRoutes.MATERIAL_EDIT,
            arguments = listOf(navArgument("resourceId") {
                type = NavType.LongType
            })
        ) {
            MaterialsCreatingNewMaterialScreen(navController = navController,
                onBackClick = { navController.popBackStack() },
                onSaveClick = { savedResourceId ->
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("newlySavedResourceId", savedResourceId)
                    navController.popBackStack()
                }

            )
        }

        composable(NavRoutes.CONFIG_LIST) {
            ConfigurationsListScreen(
                navController = navController,
                onBackClick = { navController.popBackStack() },
                onCreateClick = {
                    navController.navigate(NavRoutes.CONFIG_CREATE)
                },
                onEditClick = { configId ->
                    navController.navigate("config/edit/$configId")
                }
            )
        }

        composable(NavRoutes.CONFIG_CREATE) {
            ConfigurationSettingsScreen(navController = navController,onBackClick = { navController.popBackStack() })
        }

        composable(
            route = NavRoutes.CONFIG_EDIT,
            arguments = listOf(navArgument("configId") { type = NavType.LongType })
        ) { backStackEntry ->

            val configId = backStackEntry.arguments?.getLong("configId")

            ConfigurationSettingsScreen(navController = navController,
                onBackClick = { navController.popBackStack() },
                configId = configId
            )
        }

    }
}

@Composable
fun MainContent(
    activeConfiguration: Pair<String, String>?,
    onConfigClick: () -> Unit,
    onMaterialsClick: () -> Unit
) {
    var showInfo by remember { mutableStateOf(false) }
    var showMaterialsInfo by remember { mutableStateOf(false) }
    var showConfigInfo by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Przyjazne Słowa Ustawienia",
                            fontSize = 30.sp,
                            color = Color.White
                        )
                            IconButton(onClick = { showInfo = true }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Informacje",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                    }
                },
                backgroundColor = DarkBlue
            )
        }
    )  { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxWidth(0.9f)
            ) {
                Text(
                    text = activeConfiguration?.let {
                        "Aktywny krok uczenia: ${it.first} (tryb: ${it.second})"
                    } ?: "Brak aktywnego kroku uczenia",
                    fontSize = 25.sp
                )

                    Button(
                        onClick = onMaterialsClick,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = LightBlue2,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .width((LocalConfiguration.current.screenWidthDp * 0.7f).dp)
                            .height(85.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {

                            Text("MATERIAŁY EDUKACYJNE", fontSize = 25.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { showMaterialsInfo = true },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Informacje o materiałach edukacyjnych",
                                    tint = DarkBlue,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                Button(
                    onClick = onConfigClick,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = LightBlue2,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .width(((LocalConfiguration.current.screenWidthDp * 0.7f).dp))
                        .height(85.dp)
                ) {
                    Text("KROKI UCZENIA", fontSize = 25.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { showConfigInfo = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Informacje o krokach uczenia",
                            tint = DarkBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
    if (showMaterialsInfo) {
        AlertDialog(
            onDismissRequest = { showMaterialsInfo = false },
            title = { Text("Materiały edukacyjne") },
            text = {
                Text(
                    "Materiały edukacyjne to obrazki połączone ze słowami, które je opisują.\n\n"
                            + "W aplikacji można:\n" +
                            "- tworzyć nowe materiały (używając obrazków z galerii oraz robiąc zdjęcie w danym momencie),\n" + "" +
                            "- edytować utworzone wcześniej materiały,\n" +
                            "- usuwać materiały,\n" +
                            "- przeglądać zawartość istniejących materiałów,\n" +
                            "- wyszukiwać materiały po nazwie lub po kategorii, do której należą.\n\n"
                            + "Materiały edukacyjne tworzy się raz i można ich używać wielokrotnie w różnych krokach uczenia."
                )
            },
            confirmButton = {
                TextButton(onClick = { showMaterialsInfo = false }) {
                    Text("OK", color = DarkBlue)
                }
            }
        )
    }
    if (showConfigInfo) {
        AlertDialog(
            onDismissRequest = { showConfigInfo = false },
            title = { Text("Kroki uczenia") },
            text = {
                Text(
                    "Kroki uczenia to zestawy ćwiczeń przygotowane na podstawie wcześniej dodanych materiałów edukacyjnych.\n\n"
                    +"W każdym kroku określa się:\n" +
                            "- które materiały edukacyjne zostaną użyte,\n" +
                            "- parametry ćwiczeń, takie jak czas na odpowiedź, liczba powtórzeń itd. (dokładne ustawienia można dostosować na etapie tworzenia lub edycji kroku uczenia),\n\n"
                    +"W aplikacji można:\n" +
                            "- tworzyć kroki uczenia,\n" +
                            "- edytować istniejące kroki uczenia,\n" +
                            "- usuwać kroki uczenia,\n" +
                            "- przeglądać szczegóły kroków uczenia,\n" +
                            "- wyszukiwać kroki uczenia po nazwie,\n"+
                            "- aktywować wybrany krok w wybranym trybie pracy (nauka lub test dla dziecka),\n" +
                            "- przejść do aplikacji dla dziecka z aktywowanym krokiem uczenia w celu rozpoczęcia nauki/testu.\n\n"
                )
            },
            confirmButton = {
                TextButton(onClick = { showConfigInfo = false }) {
                    Text("OK", color = DarkBlue)
                }
            }
        )
    }
    if (showInfo) {
        AlertDialog(
            onDismissRequest = { showInfo = false },
            title = { Text("Na czym polega aplikacja?") },
            text = {
                Text(
                    "Aplikacja służy do nauki słów z wykorzystaniem obrazków.\n" +
                            "\n" +
                            "Na początku przygotowuje się materiały edukacyjne - czyli obrazki wraz ze słowami, które je opisują." +
                            "Następnie tworzy się kroki uczenia, czyli zestawy ćwiczeń, w których wybiera się materiały z listy wcześniej utworzonych oraz ustala sposób ich wykorzystania.\n" +
                            "\n" +
                            "Po utworzeniu materiałów i kroków uczenia należy aktywować ten, z którego chcemy skorzystać i wybrać tryb pracy z dzieckiem - naukę lub test.  \n" +
                            "\n" +
                            "Po aktywowaniu wybranego kroku i ustawieniu trybu wystarczy nacisnąć zielony przycisk Play znajdujący się nad listą kroków uczenia.Przycisk ten przeniesie Cię bezpośrednio do aplikacji dla dziecka. Możesz też samodzielnie otworzyć aplikację dla dziecka i tam kliknąć Play, aby rozpocząć ćwiczenia.\n\n"+
                            "W trybie nauki dorosły wspiera dziecko i pomaga mu w razie trudności.\n" +
                            "W trybie testu dorosły obserwuje samodzielne działania dziecka, aby sprawdzić jego postępy.\n"

                )
            },
            confirmButton = {
                TextButton(onClick = { showInfo = false }) {
                    Text("OK",color= DarkBlue)
                }
            }
        )
    }
}