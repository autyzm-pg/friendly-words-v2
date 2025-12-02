package com.example.friendly_words.therapist.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shared.data.entities.Configuration
import com.example.shared.data.entities.ConfigurationImageUsage
import com.example.shared.data.entities.ConfigurationResource
import com.example.shared.data.entities.Image
import com.example.shared.data.entities.LearningSettings
import com.example.shared.data.entities.Resource
import com.example.shared.data.entities.TestSettings
import com.example.shared.data.repositories.ConfigurationRepository
import com.example.shared.data.repositories.ImageRepository
import com.example.shared.data.repositories.ResourceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainScreenViewModel @Inject constructor(
    private val configurationRepository: ConfigurationRepository,
    private val resourceRepository: ResourceRepository,
    private val imageRepository: ImageRepository
) : ViewModel() {

    private val _activeConfiguration = MutableStateFlow<Configuration?>(null)
    val activeConfiguration: StateFlow<Configuration?> = _activeConfiguration

    init {
        viewModelScope.launch {
            ensureExampleMaterials()
            ensureExampleConfigurations()
            findActiveConfiguration()
        }
    }

    private suspend fun findActiveConfiguration(){
        configurationRepository.getAll().collectLatest { configs ->
            _activeConfiguration.value = configs.find { it.isActive }
        }
    }

    private suspend fun ensureExampleConfigurations() {
        val rawConfigurations = configurationRepository.getAll().first()

        if(rawConfigurations.isEmpty())
        {
            insertExampleConfigurationsWithResources()
        }
    }

    private suspend fun insertExampleConfigurationsWithResources(){
        val resources = resourceRepository.getAllOnce()
        if(resources.isEmpty()) return

        val animalResources = resources.filter { it.category == "zwierzęta" }.take(3).map { it.id }
        val toyResources = resources.filter { it.category == "zabawki" }.take(6).map { it.id }

        val resourceGroups = mapOf(
            "Krok przykładowy 1" to animalResources,
            "Krok przykładowy 2" to toyResources
        )

        val exampleConfigurations = listOf(
            Configuration(
                name = "Krok przykładowy 1",
                isActive = true,
                isExample = true,
                activeMode = "uczenie",
                learningSettings = LearningSettings(
                    numberOfWords = animalResources.size,
                    displayedImagesCount = 3,
                    repetitionPerWord = 2,
                    commandType = "{Słowo}",
                    showLabelsUnderImages = false,
                    readCommand = true,
                    hintAfterSeconds = 6,
                    typesOfHints = listOf("Obramuj poprawną", "Porusz poprawną", "Powiększ poprawną", "Wyszarz niepoprawne"),
                    typesOfPraises = listOf("dobrze", "super", "świetnie", "ekstra", "rewelacja", "brawo"),
                    animationsEnabled = true
                ),
                testSettings = TestSettings(
                    numberOfWords = animalResources.size,
                    displayedImagesCount = 3,
                    repetitionPerWord = 2,
                    commandType = "{Słowo}",
                    showLabelsUnderImages = false,
                    readCommand = true,
                    answerTimeSeconds = 6
                )
            ),
            Configuration(
                name = "Krok przykładowy 2",
                isActive = false,
                isExample = true,
                learningSettings = LearningSettings(
                    numberOfWords = toyResources.size,
                    displayedImagesCount = 6,
                    repetitionPerWord = 1,
                    commandType = "Pokaż gdzie jest {Słowo}",
                    showLabelsUnderImages = true,
                    readCommand = false,
                    hintAfterSeconds = 3,
                    typesOfHints = listOf("Wyszarz niepoprawne"),
                    typesOfPraises = listOf("dobrze", "super", "świetnie", "ekstra", "rewelacja", "brawo"),
                    animationsEnabled = true
                ),
                testSettings = TestSettings(
                    numberOfWords = toyResources.size,
                    displayedImagesCount = 6,
                    repetitionPerWord = 1,
                    commandType = "Pokaż gdzie jest {Słowo}",
                    showLabelsUnderImages = true,
                    readCommand = false,
                    answerTimeSeconds = 3
                )
            )
        )

        for (configuration in exampleConfigurations) {
            val configurationId = configurationRepository.insert(configuration)

            //dobranie odpowiedniej grupy materialow do konkretnych konfiguracji
            val selectedResources = resourceGroups[configuration.name] ?: emptyList()

            //powiazanie materialow z konfiguracja
            val links = selectedResources.map { resourceId ->
                ConfigurationResource(configurationId = configurationId, resourceId = resourceId)
            }
            configurationRepository.insertResources(links)

            //powiazanie obrazow z konfiguracja (ich wykorzystanie)
            val imageUsages = selectedResources.flatMap { resourceId ->
                val images = imageRepository.getByResourceId(resourceId)
                images.map { img ->
                    ConfigurationImageUsage(
                        configurationId = configurationId,
                        imageId = img.id,
                        inLearning = true,
                        inTest = true
                    )
                }
            }
            configurationRepository.insertImageUsages(imageUsages)

        }
    }

    private suspend fun ensureExampleMaterials() {
        val rawResources = resourceRepository.getAll().first()
        if(rawResources.isEmpty())
        {
            //dodawanie materialow przykladowych po odpaleniu aplikacji
            insertExampleMaterialsWithImages()
        }
    }

    private fun normalizeName(name: String): String {
        val map = mapOf(
            'ą' to 'a', 'ć' to 'c', 'ę' to 'e', 'ł' to 'l',
            'ń' to 'n', 'ó' to 'o', 'ś' to 's', 'ź' to 'z', 'ż' to 'z',
            'Ą' to 'A', 'Ć' to 'C', 'Ę' to 'E', 'Ł' to 'L',
            'Ń' to 'N', 'Ó' to 'O', 'Ś' to 'S', 'Ź' to 'Z', 'Ż' to 'Z'
        )
        return name.map { map[it] ?: it }.joinToString("")
    }

    private suspend fun insertExampleMaterialsWithImages()
    {
        val exampleResources = listOf(
            Resource(name = "Balon", learnedWord = "Balon", category = "zabawki", isExample = true),
            Resource(name = "Banan", learnedWord = "Banan", category = "jedzenie", isExample = true),
            Resource(name= "Bluzka", learnedWord = "Bluzka", category = "ubrania", isExample = true),
            Resource(name= "Buty", learnedWord = "Buty", category = "ubrania", isExample = true),
            Resource(name = "Bułka", learnedWord = "Bułka", category = "jedzenie", isExample = true),
            Resource(name = "Ciastko", learnedWord = "Ciastko", category = "jedzenie", isExample = true),
            Resource(name = "Ciężarówka", learnedWord = "Ciężarówka", category = "pojazdy", isExample = true),
            Resource(name = "Czapka", learnedWord = "Czapka", category = "ubrania", isExample = true),
            Resource(name = "Czesać", learnedWord = "Czesać", category = "czynności", isExample = true),
            Resource(name = "Gotować", learnedWord = "Gotować", category = "czynności", isExample = true),
            Resource(name = "Huśtać", learnedWord = "Huśtać", category = "czynności", isExample = true),
            Resource(name = "Jabłko", learnedWord = "Jabłko", category = "jedzenie", isExample = true),
            Resource(name = "Jajko", learnedWord = "Jajko", category = "jedzenie", isExample = true),
            Resource(name = "Kąpać się", learnedWord = "Kąpać się", category = "czynności", isExample = true),
            Resource(name = "Klocki", learnedWord = "Klocki", category = "zabawki", isExample = true),
            Resource(name = "Koń", learnedWord = "Koń", category = "zwierzęta", isExample = true),
            Resource(name = "Kot", learnedWord = "Kot", category = "zwierzęta", isExample = true),
            Resource(name = "Kredki", learnedWord = "Kredki", category = "zabawki", isExample = true),
            Resource(name = "Krowa", learnedWord = "Krowa", category = "zwierzęta", isExample = true),
            Resource(name = "Książka", learnedWord = "Książka", category = "zabawki", isExample = true),
            Resource(name = "Kubek", learnedWord = "Kubek", category = "przedmioty", isExample = true),
            Resource(name = "Kuchnia", learnedWord = "Kuchnia", category = "pomieszczenia", isExample = true),
            Resource(name = "Kura", learnedWord = "Kura", category = "zwierzęta", isExample = true),
            Resource(name = "Lalka", learnedWord = "Lalka", category = "zabawki", isExample = true),
            Resource(name = "Las", learnedWord = "Las", category = "miejsca", isExample = true),
            Resource(name = "Łazienka", learnedWord = "Łazienka", category = "pomieszczenia", isExample = true),
            Resource(name = "Lew", learnedWord = "Lew", category = "zwierzęta", isExample = true),
            Resource(name = "Lodówka", learnedWord = "Lodówka", category = "sprzęty", isExample = true),
            Resource(name = "Lody", learnedWord = "Lody", category = "jedzenie", isExample = true),
            Resource(name = "Morze", learnedWord = "Morze", category = "miejsca", isExample = true),
            Resource(name = "Ogórek", learnedWord = "Ogórek", category = "jedzenie", isExample = true),
            Resource(name = "Ołówek", learnedWord = "Ołówek", category = "zabawki", isExample = true),
            Resource(name = "Pies", learnedWord = "Pies", category = "zwierzęta", isExample = true),
            Resource(name = "Piłka", learnedWord = "Piłka", category = "zabawki", isExample = true),
            Resource(name = "Pociąg", learnedWord = "Pociąg", category = "pojazdy", isExample = true),
            Resource(name = "Pokój", learnedWord = "Pokój", category = "pomieszczenie", isExample = true),
            Resource(name = "Prasować", learnedWord = "Prasować", category = "czynności", isExample = true),
            Resource(name = "Rękawiczki", learnedWord = "Rękawiczki", category = "ubrania", isExample = true),
            Resource(name = "Rower", learnedWord = "Rower", category = "pojazdy", isExample = true),
            Resource(name = "Ryba", learnedWord = "Ryba", category = "zwierzęta", isExample = true),
            Resource(name = "Samochód", learnedWord = "Samochód", category = "pojazdy", isExample = true),
            Resource(name = "Samolot", learnedWord = "Samolot", category = "pojazdy", isExample = true),
            Resource(name = "Sklep", learnedWord = "Sklep", category = "miejsca", isExample = true),
            Resource(name = "Słoń", learnedWord = "Słoń", category = "zwierzęta", isExample = true),
            Resource(name = "Spać", learnedWord = "Spać", category = "czynności", isExample = true),
            Resource(name = "Spodnie", learnedWord = "Spodnie", category = "ubrania", isExample = true),
            Resource(name = "Stół", learnedWord = "Stół", category = "meble", isExample = true),
            Resource(name = "Telewizor", learnedWord = "Telewizor", category = "sprzęty", isExample = true),
            Resource(name = "Woda", learnedWord = "Woda", category = "napoje", isExample = true),
            Resource(name = "Żaba", learnedWord = "Żaba", category = "zwierzęta", isExample = true)
        )

        for (resource in exampleResources)
        {
            val resourceId = resourceRepository.insert(resource)

            val baseName = normalizeName(resource.name.lowercase().replace(" ", "_"))
            val imagePaths = (1..3).map { index ->
                "file:///android_asset/exemplary_photos/${baseName}_${index}.png"
            }

            val images = imagePaths.map { path -> Image(path = path) }
            val insertedImageIds = imageRepository.insertMany(images)
            imageRepository.linkImagesToResource(resourceId, insertedImageIds)
        }
    }
}
