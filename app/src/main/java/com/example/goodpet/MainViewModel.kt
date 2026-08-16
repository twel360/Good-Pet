package com.example.goodpet

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.goodpet.data.PetRepository
import com.example.goodpet.data.model.PetProfile
import com.example.goodpet.data.model.PetVoice
import com.example.goodpet.util.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PetRepository(application)
    private val ttsManager = TtsManager(application)

    var pets by mutableStateOf<List<PetProfile>>(emptyList())
    var customPhrases by mutableStateOf<List<String>>(emptyList())
    var selectedPetId by mutableStateOf<String?>(null)
    var selectedVoiceName by mutableStateOf<String?>(null)

    var availableVoices by mutableStateOf<List<PetVoice>>(emptyList())
        private set
    
    var isTtsReady by mutableStateOf(false)
        private set
    
    var initializationError by mutableStateOf<String?>(null)
        private set

    private var isInitializing = false
    private var initTimeoutJob: Job? = null

    init {
        loadData()
        selectedVoiceName = repository.getSelectedVoiceName()
    }

    fun loadData() {
        pets = repository.loadPets()
        if (selectedPetId == null && pets.isNotEmpty()) {
            selectedPetId = pets.first().id
        }
        selectedPetId?.let { id ->
            customPhrases = repository.loadPhrases(id)
        }
    }

    fun onPetSelected(petId: String) {
        selectedPetId = petId
        customPhrases = repository.loadPhrases(petId)
    }

    fun initTts(context: Context) {
        if (isTtsReady || isInitializing) return

        isInitializing = true
        initializationError = null

        initTimeoutJob?.cancel()
        initTimeoutJob = viewModelScope.launch {
            delay(6000)
            if (!isTtsReady) {
                isInitializing = false
                initializationError = "Text-to-Speech initialization is taking a long time. This can happen on some devices during cold starts."
            }
        }

        ttsManager.initialize { success, error ->
            isInitializing = false
            if (success) {
                availableVoices = ttsManager.availableVoices
                isTtsReady = true
                initializationError = null
                initTimeoutJob?.cancel()
            } else {
                initializationError = error
            }
        }
    }

    fun speak(textToSay: String, pitch: Float, speed: Float, voiceName: String?) {
        ttsManager.speak(textToSay, pitch, speed, voiceName)
    }

    fun resetAppData() {
        repository.clearData()
        loadData()
        initTts(getApplication())
    }

    fun selectVoice(name: String) {
        selectedVoiceName = name
    }

    fun saveVoiceSettings(pitch: Float, speed: Float) {
        repository.saveVoiceSettings(pitch, speed, selectedVoiceName)
    }

    fun savePets(updatedPets: List<PetProfile>) {
        pets = updatedPets
        viewModelScope.launch {
            repository.savePets(updatedPets)
        }
    }

    fun savePhrases(updatedPhrases: List<String>) {
        val id = selectedPetId ?: return
        customPhrases = updatedPhrases
        viewModelScope.launch {
            repository.savePhrases(id, updatedPhrases)
        }
    }

    fun resetPhrases() {
        savePhrases(repository.defaultPhrases)
    }

    override fun onCleared() {
        ttsManager.stop()
        ttsManager.shutdown()
        super.onCleared()
    }
}
