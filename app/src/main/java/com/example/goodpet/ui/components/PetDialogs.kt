package com.example.goodpet.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.goodpet.data.model.PetGender
import com.example.goodpet.data.model.PetProfile
import com.example.goodpet.data.model.PetVoice
import com.example.goodpet.data.model.formatPhrase
import java.util.Locale

@Composable
fun EditPetDialog(
    pet: PetProfile,
    allPets: List<PetProfile>,
    onDismiss: () -> Unit,
    onSave: (String, PetGender) -> Unit,
    onDelete: () -> Unit,
    onSetPhoto: (Uri) -> Unit,
    onRemovePhoto: () -> Unit
) {
    var editName by remember(pet) { mutableStateOf(pet.name) }
    var editGender by remember(pet) { mutableStateOf(pet.gender) }
    val context = LocalContext.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { onSetPhoto(it) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Pet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = { Text("Pet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Gender / Pronouns:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PetGender.entries.forEach { gender ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { editGender = gender }
                        ) {
                            RadioButton(
                                selected = editGender == gender,
                                onClick = { editGender = gender }
                            )
                            Text(gender.displayName, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (pet.imageUri == null) "Set Background Photo" else "Change Background Photo")
                }

                if (pet.imageUri != null) {
                    TextButton(
                        onClick = onRemovePhoto,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Remove Photo", color = MaterialTheme.colorScheme.error)
                    }
                }

                if (allPets.size > 1) {
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Delete Pet Profile", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (editName.isNotBlank()) {
                        onSave(editName.trim(), editGender)
                    }
                }
            ) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddPetDialog(
    onDismiss: () -> Unit,
    onAdd: (String, PetGender) -> Unit
) {
    var newPetName by remember { mutableStateOf("") }
    var selectedGender by remember { mutableStateOf(PetGender.BOY) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Pet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newPetName,
                    onValueChange = { newPetName = it },
                    label = { Text("Pet Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Gender / Pronouns:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PetGender.entries.forEach { gender ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { selectedGender = gender }
                        ) {
                            RadioButton(
                                selected = selectedGender == gender,
                                onClick = { selectedGender = gender }
                            )
                            Text(gender.displayName, fontSize = 13.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPetName.isNotBlank()) {
                        onAdd(newPetName.trim(), selectedGender)
                    }
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun PhraseListDialog(
    pet: PetProfile,
    phrases: List<String>,
    currentPhraseIndex: Int,
    onDismiss: () -> Unit,
    onPhraseSelected: (Int) -> Unit,
    onDeletePhrase: (Int) -> Unit,
    onResetPhrases: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Phrases for ${pet.name}")
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(phrases) { index, phrase ->
                    val formatted = formatPhrase(phrase, pet)
                    val isSelected = index == currentPhraseIndex

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPhraseSelected(index) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatted,
                                modifier = Modifier.weight(1f),
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            if (phrases.size > 1) {
                                IconButton(
                                    onClick = { onDeletePhrase(index) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Delete phrase",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onResetPhrases) {
                    Text("Reset to Defaults", color = MaterialTheme.colorScheme.error)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun VoiceSettingsDialog(
    availableVoices: List<PetVoice>,
    selectedVoiceName: String?,
    pitch: Float,
    speed: Float,
    onDismiss: () -> Unit,
    onPitchChange: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onVoiceSelect: (String) -> Unit,
    onSave: () -> Unit,
    onTest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RecordVoiceOver, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voice Controls")
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text("Pitch (${String.format(Locale.US, "%.1fx", pitch)})", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = pitch,
                        onValueChange = onPitchChange,
                        valueRange = 0.5f..2.0f
                    )
                }

                item {
                    Text("Speed (${String.format(Locale.US, "%.1fx", speed)})", fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = speed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.5f..2.0f
                    )
                }

                if (availableVoices.isNotEmpty()) {
                    item {
                        Text("Installed Voice Engine", fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    items(availableVoices, key = { it.name }) { voice ->
                        val isSelected = selectedVoiceName == voice.name ||
                                       (selectedVoiceName == null && availableVoices.firstOrNull()?.name == voice.name)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVoiceSelect(voice.name) }
                                .padding(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = voice.displayName,
                                fontSize = 16.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onTest) { Text("Test") }
        }
    )
}

@Composable
fun AddPhraseDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var newPhrase by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Phrase") },
        text = {
            Column {
                Text(
                    "Tap a tag below to insert it:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val tags = listOf(
                        "{name}",
                        "{good_boy_girl}",
                        "{handsome_pretty}",
                        "{he_she_they}",
                        "{him_her_them}",
                        "{his_her_their}"
                    )
                    items(tags) { tag ->
                        AssistChip(
                            onClick = {
                                val space = if (newPhrase.isNotEmpty() && !newPhrase.endsWith(" ")) " " else ""
                                newPhrase = newPhrase + space + tag
                            },
                            label = { Text(tag, fontSize = 12.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newPhrase,
                    onValueChange = { newPhrase = it },
                    label = { Text("Enter your phrase...") },
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (newPhrase.isNotBlank()) {
                        onSave(newPhrase.trim())
                    }
                }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun HelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("How to use Good Pet")
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    Text(
                        "Welcome! This app helps you praise your pets using high quality voices. Here's a quick guide:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                item {
                    HelpSection("🐾 Managing Pets", "Tap '+' to add a pet. Tap a pet's name to switch who you're praising. Use the edit icon to change their photo or gender.")
                }

                item {
                    HelpSection("🎲 Quick Praise", "Use 'Random Praise' to pick a saved phrase, or 'Surprise Me' to add a brand new cute compliment to your library.")
                }

                item {
                    HelpSection("📝 Custom Phrases", "Tap 'List' to manage your library. Tap 'Add' to write your own! Use the smart tags like {name} or {good_boy_girl} to keep it personalized.")
                }

                item {
                    HelpSection("🗣️ Voice Controls", "Choose from 30+ accents. HD voices sound best but need internet. Adjust pitch and speed to find the perfect 'parent' voice.")
                }

                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("🔒 Privacy First", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("All your data (pet names, custom phrases, and photos) is stored locally on this phone. We never upload your data to any servers.", fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Got it!") }
        }
    )
}
