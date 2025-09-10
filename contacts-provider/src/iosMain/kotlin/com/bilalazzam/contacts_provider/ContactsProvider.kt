package com.bilalazzam.contacts_provider


import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.autoreleasepool
import platform.Contacts.*
import platform.UIKit.UIImage

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ContactsProvider {
    private val contactsCache: ContactsCache = ContactsCache()

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
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
            val imageData = cnContact?.imageData

            val avatar = autoreleasepool {
                if (imageData != null) {
                    val uiImage = UIImage(data = imageData)
                    if (uiImage != null) {
                        ContactAvatar.AvatarBitmap(uiImage.toImageBitmap())
                    } else {
                        ContactAvatar.None
                    }
                } else {
                    ContactAvatar.None
                }
            }

            contacts.add(
                Contact(
                    id = cnContact?.identifier,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumbers = numbers,
                    avatar = avatar
                )
            )
        }

        // Save contacts to cache after successful fetch
        contactsCache.saveContacts(contacts)

        return contacts
    }

    actual fun getCachedContacts(): List<Contact> {
        return contactsCache.getContacts()
    }

    private fun CNContact.getPhoneNumbers(): List<String> {
        return phoneNumbers.mapNotNull { labeledValue ->
            (labeledValue as? CNLabeledValue)?.value.let { it as? CNPhoneNumber }?.stringValue
        }
    }
}
