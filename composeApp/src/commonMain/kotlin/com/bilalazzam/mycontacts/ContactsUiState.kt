package com.bilalazzam.mycontacts

import com.bilalazzam.contacts_provider.Contact

data class ContactsUiState(
    val id: String,
    val name: String,
    val phone: String,
    val isMenaUser: Boolean,
    val imageUrl: String?,
)

fun Contact.toUiState(): ContactsUiState {
    return ContactsUiState(
        id = id.orEmpty(),
        name = firstName.orEmpty() + " " + lastName.orEmpty(),
        phone = phoneNumbers.firstOrNull().orEmpty(),
        isMenaUser = true,
        imageUrl = ""
    )
}