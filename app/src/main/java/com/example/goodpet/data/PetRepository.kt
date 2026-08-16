package com.example.goodpet.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.edit
import com.example.goodpet.data.model.PetGender
import com.example.goodpet.data.model.PetProfile
import org.json.JSONArray
import org.json.JSONObject

class PetRepository(context: Context) {
    private val sharedPrefs: SharedPreferences =
        context.getSharedPreferences("pet_praise_prefs", Context.MODE_PRIVATE)

    val defaultPhrases = listOf(
        "{name} is the best {good_boy_girl} ever!",
        "{name} deserves all the treats!",
        "10 out of 10 would pet {name} again.",
        "{name} is an absolute angel!",
        "World's most {handsome_pretty}: {name}!",
        "Look at {name}, {he_she_they} did such a great job!"
    )

    fun loadPets(): List<PetProfile> {
        val list = mutableListOf<PetProfile>()
        try {
            val raw = sharedPrefs.getString("pets_data", null) ?: return emptyList()
            val array = JSONArray(raw)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", System.currentTimeMillis().toString() + i)
                val name = obj.optString("name", "Pet")
                val uriStr = if (obj.isNull("imageUri")) null else obj.optString("imageUri", null)

                val validatedUri = if (uriStr != null) {
                    try {
                        Uri.parse(uriStr)
                        uriStr
                    } catch (_: Exception) {
                        null
                    }
                } else null

                val genderName = obj.optString("gender", PetGender.BOY.name)
                val gender = try { PetGender.valueOf(genderName) } catch (_: Exception) { PetGender.BOY }

                list.add(PetProfile(id = id, name = name, gender = gender, imageUri = validatedUri))
            }
        } catch (_: Exception) {}
        return list
    }

    fun savePets(pets: List<PetProfile>) {
        try {
            val array = JSONArray()
            for (pet in pets) {
                val obj = JSONObject()
                obj.put("id", pet.id)
                obj.put("name", pet.name)
                obj.put("gender", pet.gender.name)
                obj.put("imageUri", pet.imageUri ?: JSONObject.NULL)
                array.put(obj)
            }
            sharedPrefs.edit { putString("pets_data", array.toString()) }
        } catch (_: Exception) {}
    }

    fun loadPhrases(petId: String): List<String> {
        return try {
            val set = sharedPrefs.getStringSet("phrases_$petId", null)
            if (set != null) {
                set.toList()
            } else {
                // Migration/Fallback: try the old global key or use defaults
                val globalSet = sharedPrefs.getStringSet("phrases_data", null)
                globalSet?.toList() ?: defaultPhrases
            }
        } catch (_: Exception) {
            defaultPhrases
        }
    }

    fun savePhrases(petId: String, phrases: List<String>) {
        try {
            sharedPrefs.edit { putStringSet("phrases_$petId", phrases.toSet()) }
        } catch (_: Exception) {}
    }

    fun clearData() {
        sharedPrefs.edit { clear() }
    }

    fun getVoicePitch() = sharedPrefs.getFloat("voice_pitch", 1.0f)
    fun getVoiceSpeed() = sharedPrefs.getFloat("voice_speed", 1.0f)
    fun getSelectedVoiceName() = sharedPrefs.getString("selected_voice_name", null)

    fun saveVoiceSettings(pitch: Float, speed: Float, voiceName: String?) {
        sharedPrefs.edit {
            putFloat("voice_pitch", pitch)
            putFloat("voice_speed", speed)
            putString("selected_voice_name", voiceName)
        }
    }
}
