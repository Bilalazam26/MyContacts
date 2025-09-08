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
            CNContactPhoneNumbersKey,
            CNContactImageDataKey
        )
        val request = CNContactFetchRequest(keysToFetch = keysToFetch)
        val contacts = mutableListOf<Contact>()

        store.enumerateContactsWithFetchRequest(request, error = null) { cnContact, _ ->
            val firstName = cnContact?.givenName ?: ""
            val lastName = cnContact?.familyName ?: ""
            val numbers = cnContact?.getPhoneNumbers() ?: emptyList()
            contacts.add(Contact(cnContact?.identifier, firstName, lastName, numbers))
        }

        return contacts
    }

    private fun CNContact.getPhoneNumbers(): List<String> {
        return phoneNumbers.mapNotNull { labeledValue ->
            (labeledValue as? CNLabeledValue)?.value.let { it as? CNPhoneNumber }?.stringValue
        }
    }
}