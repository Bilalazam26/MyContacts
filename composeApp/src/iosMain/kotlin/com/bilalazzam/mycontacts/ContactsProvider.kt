package com.bilalazzam.mycontacts


import kotlinx.cinterop.ExperimentalForeignApi
import platform.Contacts.*

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ContactsProvider {
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun getAllContacts(): List<Contact> {
        val store = CNContactStore()
        val keysToFetch = listOf(
            CNContactIdentifierKey,
            CNContactGivenNameKey,
            CNContactFamilyNameKey,
            CNContactPhoneNumbersKey
        )
        val request = CNContactFetchRequest(keysToFetch = keysToFetch)
        val contacts = mutableListOf<Contact>()

        store.enumerateContactsWithFetchRequest(request, error = null) { cnContact, _ ->
            val name = "${cnContact?.givenName} ${cnContact?.familyName}".trim()
            val numbers = cnContact?.getPhoneNumbers() ?: emptyList()
            contacts.add(Contact(cnContact?.identifier, name, numbers))
        }

        return contacts
    }

    private fun CNContact.getPhoneNumbers(): List<String> {
        return phoneNumbers.mapNotNull { lv ->
            (lv as? CNLabeledValue)?.value.let { it as? CNPhoneNumber }?.stringValue
        }
    }
}