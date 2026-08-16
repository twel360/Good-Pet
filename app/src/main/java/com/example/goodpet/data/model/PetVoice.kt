package com.example.goodpet.data.model

import android.speech.tts.Voice
import java.util.Locale

data class PetVoice(
    val name: String,
    val displayName: String,
    val locale: Locale,
    val isNetwork: Boolean,
    val voice: Voice
)
