package com.example.goodpet.data.model

enum class PetGender(val displayName: String) {
    BOY("Boy"),
    GIRL("Girl"),
    UNSPECIFIED("Unspecified")
}

data class PetProfile(
    val id: String,
    val name: String,
    val gender: PetGender = PetGender.BOY,
    val imageUri: String? = null
)

fun formatPhrase(template: String, pet: PetProfile): String {
    val goodBoyGirl = when (pet.gender) {
        PetGender.BOY -> "good boy"
        PetGender.GIRL -> "good girl"
        PetGender.UNSPECIFIED -> "good pet"
    }

    val handsomePretty = when (pet.gender) {
        PetGender.BOY -> "handsome boy"
        PetGender.GIRL -> "pretty girl"
        PetGender.UNSPECIFIED -> "cutie"
    }

    val heSheThey = when (pet.gender) {
        PetGender.BOY -> "he"
        PetGender.GIRL -> "she"
        PetGender.UNSPECIFIED -> "they"
    }

    val himHerThem = when (pet.gender) {
        PetGender.BOY -> "him"
        PetGender.GIRL -> "her"
        PetGender.UNSPECIFIED -> "them"
    }

    val hisHerTheir = when (pet.gender) {
        PetGender.BOY -> "his"
        PetGender.GIRL -> "her"
        PetGender.UNSPECIFIED -> "their"
    }

    return template
        .replace("{name}", pet.name)
        .replace("{good_boy_girl}", goodBoyGirl)
        .replace("{handsome_pretty}", handsomePretty)
        .replace("{he_she_they}", heSheThey)
        .replace("{him_her_them}", himHerThem)
        .replace("{his_her_their}", hisHerTheir)
}
