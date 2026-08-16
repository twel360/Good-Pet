package com.example.goodpet.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.example.goodpet.data.model.PetVoice
import java.util.Locale

class TtsManager(private val context: Context) {
    private var tts: TextToSpeech? = null
    
    var isReady = false
        private set
    
    var availableVoices = emptyList<PetVoice>()
        private set

    fun initialize(onResult: (Boolean, String?) -> Unit) {
        if (tts != null && isReady) {
            onResult(true, null)
            return
        }

        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                try {
                    tts?.language = Locale.US
                    availableVoices = getNamedVoices(tts?.voices)
                    isReady = true
                    onResult(true, null)
                } catch (e: Exception) {
                    onResult(false, "Error during voice setup: ${e.localizedMessage}")
                }
            } else {
                onResult(false, "Failed to initialize TTS engine (Error code: $status)")
            }
        }
    }

    fun speak(text: String, pitch: Float, speed: Float, voiceName: String?) {
        if (!isReady) return
        try {
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speed)
            voiceName?.let { name ->
                availableVoices.find { it.name == name }?.let { 
                    tts?.voice = it.voice 
                }
            }
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "PetPraiseID")
        } catch (_: Exception) {}
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
    }

    private fun getNamedVoices(ttsVoices: Set<Voice>?): List<PetVoice> {
        if (ttsVoices == null) return emptyList()

        val femaleNamesPool = mapOf(
            "US" to listOf("Emma", "Olivia", "Sophia", "Ava", "Isabella", "Mia", "Harper", "Evelyn", "Abigail", "Emily", "Madison", "Scarlett", "Chloe", "Sofia", "Grace"),
            "GB" to listOf("Charlotte", "Amelia", "Isla", "Ruby", "Poppy", "Alice", "Florence", "Daisy", "Freya", "Phoebe", "Sienna", "Willow", "Ivy", "Grace", "Sophie"),
            "AU" to listOf("Mia", "Chloe", "Grace", "Zoe", "Ella", "Charlotte", "Amelia", "Isla", "Ava", "Harper"),
            "IE" to listOf("Saoirse", "Siobhan", "Aoife", "Niamh", "Roisin", "Clodagh", "Eimear", "Orla", "Sinead", "Tara"),
            "CA" to listOf("Maya", "Lily", "Sarah", "Hannah", "Zoe", "Alice", "Emily", "Lea", "Emma", "Mila")
        )
        val maleNamesPool = mapOf(
            "US" to listOf("Oliver", "Liam", "Sulley", "Noah", "James", "Benjamin", "Mikey", "Sean", "William", "Lucas", "Henry", "Theodore", "Jack", "Levi", "Alexander"),
            "GB" to listOf("Arthur", "George", "Harry", "Angus", "Oscar", "Leo", "Thomas", "Archie", "Henry", "Jack", "Freddie", "Charlie", "Alf", "Teddy", "Theo"),
            "AU" to listOf("Jack", "William", "Lachlan", "Thomas", "James", "Oliver", "Noah", "Ethan", "Lucas", "Cooper"),
            "IE" to listOf("Seamus", "Finn", "Liam", "Connor", "Sean", "Cillian", "Darragh", "Eoghan", "Oisin", "Patrick"),
            "CA" to listOf("Logan", "Lucas", "Ethan", "Noah", "Owen", "Jackson", "Aiden", "Jacob", "Liam", "Mason")
        )
        val unisexNames = listOf("Alex", "Jordan", "Charlie", "Casey", "Taylor", "Riley", "Skylar", "Peyton", "Quinn")

        val result = mutableListOf<PetVoice>()
        val englishVoices = ttsVoices.filter { 
            val lang = it.locale.language.lowercase()
            (lang == "en" || lang == "eng") && it.locale.country != "IN" 
        }

        val localeGroups = englishVoices.groupBy { it.locale.country }

        localeGroups.forEach { (country, voices) ->
            val accentName = when (country) {
                "US" -> "US"
                "GB" -> "UK"
                "AU" -> "AU"
                "IE" -> "Ireland"
                "CA" -> "Canada"
                else -> if (country.isEmpty()) "General" else country
            }

            fun isFemale(name: String) = name.contains("female", true) || 
                    name.contains("-f-", true) || name.contains("_f_", true) ||
                    name.contains("variant-f", true) || name.contains("-f", true)

            fun isMale(name: String) = name.contains("male", true) || 
                    name.contains("-m-", true) || name.contains("_m_", true) ||
                    name.contains("variant-m", true) || name.contains("-m", true)

            val sortedVoices = voices.sortedByDescending { it.isNetworkConnectionRequired }
            
            val identifiedFemales = sortedVoices.filter { isFemale(it.name) }.toMutableList()
            val identifiedMales = sortedVoices.filter { isMale(it.name) && !isFemale(it.name) }.toMutableList()
            val neutrals = sortedVoices.filter { it !in identifiedFemales && it !in identifiedMales }.toMutableList()

            neutrals.forEach { voice ->
                if (identifiedFemales.size <= identifiedMales.size) {
                    identifiedFemales.add(voice)
                } else {
                    identifiedMales.add(voice)
                }
            }

            val halfLimit = 5
            val targetSize = minOf(identifiedFemales.size, identifiedMales.size).coerceAtMost(halfLimit)

            identifiedFemales.take(targetSize).forEachIndexed { index, voice ->
                val pool = femaleNamesPool[country] ?: unisexNames
                val baseName = if (index < pool.size) pool[index] else "Female ${index + 1}"
                result.add(createPetVoice(voice, baseName, accentName, "Female"))
            }

            identifiedMales.take(targetSize).forEachIndexed { index, voice ->
                val pool = maleNamesPool[country] ?: unisexNames
                val baseName = if (index < pool.size) pool[index] else "Male ${index + 1}"
                result.add(createPetVoice(voice, baseName, accentName, "Male"))
            }
        }

        return result.sortedWith(
            compareByDescending<PetVoice> { it.isNetwork }
            .thenByDescending { it.locale.country == "US" }
            .thenByDescending { it.locale.country == "GB" }
            .thenBy { it.displayName }
        )
    }

    private fun createPetVoice(voice: Voice, baseName: String, accentName: String, genderLabel: String): PetVoice {
        var flavorText = ""
        if (baseName == "Sulley") flavorText = " (Boston)"
        if (baseName == "Angus") flavorText = " (Scottish)"
        
        val typeTag = if (voice.isNetworkConnectionRequired) "HD" else "Off"
        val displayName = "$baseName ($accentName $genderLabel)$flavorText [$typeTag]"
        return PetVoice(voice.name, displayName, voice.locale, voice.isNetworkConnectionRequired, voice)
    }
}
