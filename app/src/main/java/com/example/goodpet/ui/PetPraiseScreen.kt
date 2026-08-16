package com.example.goodpet.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.goodpet.MainViewModel
import com.example.goodpet.data.model.PetGender
import com.example.goodpet.data.model.PetProfile
import com.example.goodpet.data.model.formatPhrase
import com.example.goodpet.ui.components.*

@Composable
fun PetPraiseScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("pet_praise_prefs", Context.MODE_PRIVATE) }

    val pets = viewModel.pets.ifEmpty {
        listOf(PetProfile(id = "1", name = "Frankie", gender = PetGender.BOY, imageUri = null))
    }

    val customPhrases = viewModel.customPhrases

    val phraseSuggestions = remember {
        listOf(
            "Who's a {good_boy_girl}? {name} is!",
            "{name} is such a smart {good_boy_girl}.",
            "I love {name} so much!",
            "{name} has the cutest little face.",
            "Give {name} a big hug for being so good!",
            "{name} is the light of my life.",
            "What a majestic {good_boy_girl} {name} is!",
            "{name} is doing a great job!",
            "{name} is the champion of nap time.",
            "Everyone say hi to {name}, the best {good_boy_girl}!",
            "{name} deserves a thousand belly rubs.",
            "Is it treat time? I think {name} says yes!",
            "I'm so proud of {name}.",
            "{name} is the most precious {good_boy_girl}.",
            "11 out of 10 for {name}!",
            "Such a polite {good_boy_girl}.",
            "{name} makes every day better!"
        )
    }

    val selectedPetId = viewModel.selectedPetId ?: (if (pets.isNotEmpty()) pets.first().id else "1")
    val currentPet = pets.find { it.id == selectedPetId } ?: if (pets.isNotEmpty()) pets.first() else PetProfile("1", "Pet", PetGender.BOY, null)

    var currentPhraseIndex by remember { mutableIntStateOf(0) }
    var showAddPetDialog by remember { mutableStateOf(false) }
    var showEditPetDialog by remember { mutableStateOf(false) }
    var showAddPhraseDialog by remember { mutableStateOf(false) }
    var showVoiceSettingsDialog by remember { mutableStateOf(false) }
    var showPhraseListDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    var voicePitch by remember { mutableFloatStateOf(sharedPrefs.getFloat("voice_pitch", 1.0f)) }
    var voiceSpeed by remember { mutableFloatStateOf(sharedPrefs.getFloat("voice_speed", 1.0f)) }

    val currentText = if (customPhrases.isNotEmpty()) {
        val safeIndex = currentPhraseIndex.coerceIn(0, customPhrases.size - 1)
        formatPhrase(customPhrases[safeIndex], currentPet)
    } else {
        formatPhrase("{name} is a {good_boy_girl}!", currentPet)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentPet.imageUri != null) {
            AsyncImage(
                model = currentPet.imageUri,
                contentDescription = "Pet Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF3A6073), Color(0xFF16222F))
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "EVERY PET IS A GOOD PET",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = { showHelpDialog = true },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(32.dp),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = "Help", modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(pets, key = { it.id }) { pet ->
                        val genderTag = when (pet.gender) {
                            PetGender.BOY -> " ♂"
                            PetGender.GIRL -> " ♀"
                            PetGender.UNSPECIFIED -> ""
                        }
                        FilterChip(
                            selected = pet.id == selectedPetId,
                            onClick = { 
                                viewModel.onPetSelected(pet.id)
                                currentPhraseIndex = 0
                            },
                            label = { Text("${pet.name}$genderTag") },
                            leadingIcon = {
                                Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                labelColor = Color.White,
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    item {
                        IconButton(
                            onClick = { showEditPetDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Active Pet", modifier = Modifier.size(18.dp))
                        }
                    }
                    item {
                        IconButton(
                            onClick = { showAddPetDialog = true },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Pet", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Central Phrase Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clickable {
                            viewModel.speak(currentText, voicePitch, voiceSpeed, viewModel.selectedVoiceName)
                        }
                ) {
                    Text(
                        text = currentText,
                        color = Color.White,
                        fontSize = 30.sp,
                        lineHeight = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(
                        onClick = {
                            if (customPhrases.isNotEmpty()) {
                                currentPhraseIndex = if (currentPhraseIndex <= 0) customPhrases.size - 1 else currentPhraseIndex - 1
                                val spoken = formatPhrase(customPhrases[currentPhraseIndex], currentPet)
                                viewModel.speak(spoken, voicePitch, voiceSpeed, viewModel.selectedVoiceName)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.3f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Phrase")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = if (customPhrases.isNotEmpty()) "${currentPhraseIndex + 1} / ${customPhrases.size}" else "1 / 1",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = {
                            if (customPhrases.isNotEmpty()) {
                                currentPhraseIndex = (currentPhraseIndex + 1) % customPhrases.size
                                val spoken = formatPhrase(customPhrases[currentPhraseIndex], currentPet)
                                viewModel.speak(spoken, voicePitch, voiceSpeed, viewModel.selectedVoiceName)
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.3f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Phrase")
                    }
                }
            }

            // Bottom Action Buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        if (customPhrases.isNotEmpty()) {
                            val nextIndex = (customPhrases.indices).random()
                            currentPhraseIndex = nextIndex
                            val phraseToSpeak = formatPhrase(customPhrases[nextIndex], currentPet)
                            viewModel.speak(phraseToSpeak, voicePitch, voiceSpeed, viewModel.selectedVoiceName)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Random Praise!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val unused = phraseSuggestions.filter { it !in customPhrases }
                        val newPhrase = if (unused.isNotEmpty()) unused.random() else phraseSuggestions.random()

                        val updatedPhrases = customPhrases + newPhrase
                        viewModel.savePhrases(updatedPhrases)
                        currentPhraseIndex = updatedPhrases.size - 1
                        viewModel.speak(formatPhrase(newPhrase, currentPet), voicePitch, voiceSpeed, viewModel.selectedVoiceName)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Surprise Me!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showPhraseListDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("List")
                    }

                    OutlinedButton(
                        onClick = { showAddPhraseDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add")
                    }

                    OutlinedButton(
                        onClick = { showVoiceSettingsDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Voice")
                    }
                }
            }
        }
    }

    if (showEditPetDialog) {
        EditPetDialog(
            pet = currentPet,
            allPets = pets,
            onDismiss = { showEditPetDialog = false },
            onSave = { name, gender ->
                val updated = pets.map { pet ->
                    if (pet.id == currentPet.id) pet.copy(name = name, gender = gender) else pet
                }
                viewModel.savePets(updated)
                showEditPetDialog = false
            },
            onDelete = {
                val updated = pets.filter { it.id != currentPet.id }
                viewModel.savePets(updated)
                if (updated.isNotEmpty()) {
                    viewModel.onPetSelected(updated.first().id)
                    currentPhraseIndex = 0
                }
                showEditPetDialog = false
            },
            onSetPhoto = { uri ->
                try {
                    val flag = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(uri, flag)
                } catch (_: Exception) {}

                val updatedPets = pets.map { pet ->
                    if (pet.id == currentPet.id) pet.copy(imageUri = uri.toString()) else pet
                }
                viewModel.savePets(updatedPets)
            },
            onRemovePhoto = {
                val updated = pets.map { pet ->
                    if (pet.id == currentPet.id) pet.copy(imageUri = null) else pet
                }
                viewModel.savePets(updated)
            }
        )
    }

    if (showAddPetDialog) {
        AddPetDialog(
            onDismiss = { showAddPetDialog = false },
            onAdd = { name, gender ->
                val newPet = PetProfile(
                    id = System.currentTimeMillis().toString(),
                    name = name,
                    gender = gender
                )
                val updated = pets + newPet
                viewModel.savePets(updated)
                viewModel.onPetSelected(newPet.id)
                currentPhraseIndex = 0
                showAddPetDialog = false
            }
        )
    }

    if (showPhraseListDialog) {
        PhraseListDialog(
            pet = currentPet,
            phrases = customPhrases,
            currentPhraseIndex = currentPhraseIndex,
            onDismiss = { showPhraseListDialog = false },
            onPhraseSelected = { index ->
                currentPhraseIndex = index
                viewModel.speak(formatPhrase(customPhrases[index], currentPet), voicePitch, voiceSpeed, viewModel.selectedVoiceName)
                showPhraseListDialog = false
            },
            onDeletePhrase = { index ->
                val updated = customPhrases.toMutableList()
                updated.removeAt(index)
                viewModel.savePhrases(updated)
                if (currentPhraseIndex >= updated.size) {
                    currentPhraseIndex = updated.size - 1
                }
            },
            onResetPhrases = { viewModel.resetPhrases() }
        )
    }

    if (showVoiceSettingsDialog) {
        VoiceSettingsDialog(
            availableVoices = viewModel.availableVoices,
            selectedVoiceName = viewModel.selectedVoiceName,
            pitch = voicePitch,
            speed = voiceSpeed,
            onDismiss = { showVoiceSettingsDialog = false },
            onPitchChange = { voicePitch = it },
            onSpeedChange = { voiceSpeed = it },
            onVoiceSelect = { name ->
                viewModel.selectVoice(name)
                viewModel.speak("I am a good pet!", voicePitch, voiceSpeed, name)
            },
            onSave = {
                viewModel.saveVoiceSettings(voicePitch, voiceSpeed)
                showVoiceSettingsDialog = false
            },
            onTest = {
                viewModel.speak("I am a good pet!", voicePitch, voiceSpeed, viewModel.selectedVoiceName)
            }
        )
    }

    if (showAddPhraseDialog) {
        AddPhraseDialog(
            onDismiss = { showAddPhraseDialog = false },
            onSave = { phrase ->
                val formatted = if (phrase.contains("{name}")) phrase else "{name} $phrase"
                val updated = customPhrases + formatted.trim()
                viewModel.savePhrases(updated)
                showAddPhraseDialog = false
            }
        )
    }

    if (showHelpDialog) {
        HelpDialog(onDismiss = { showHelpDialog = false })
    }
}
