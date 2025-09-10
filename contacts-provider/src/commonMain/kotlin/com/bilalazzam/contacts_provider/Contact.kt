package com.bilalazzam.contacts_provider

import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class Contact(
    val id: String?,
    val firstName: String?,
    val lastName: String?,
    val phoneNumbers: List<String>,
    val avatar: ContactAvatar = ContactAvatar.None
) {
    val displayName: String
        get() = when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() -> "$firstName $lastName"
            !firstName.isNullOrBlank() -> firstName
            !lastName.isNullOrBlank() -> lastName
            else -> "Unknown"
        }

    val initials: String
        get() = when {
            !firstName.isNullOrBlank() && !lastName.isNullOrBlank() ->
                "${firstName.firstOrNull()}${lastName.firstOrNull()}".uppercase()
            !firstName.isNullOrBlank() ->
                firstName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            !lastName.isNullOrBlank() ->
                lastName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            else -> "?"
        }

    val hasPhoneNumbers: Boolean
        get() = phoneNumbers.isNotEmpty()
}

@Serializable
sealed class ContactAvatar {
    @Serializable
    data class AvatarUri(val uri: String): ContactAvatar()

    @Serializable
    data class AvatarBitmap(@Contextual val bitmap: ImageBitmap): ContactAvatar()

    @Serializable
    data object None: ContactAvatar()
}