package com.bilalazzam.mycontacts


import android.content.Context
import android.provider.ContactsContract

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ContactsProvider(private val context: Context) {
    actual suspend fun getAllContacts(): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val resolver = context.contentResolver

        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)) ?: ""

                val phoneNumbers = mutableListOf<String>()
                if (it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val phoneCursor = resolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                        arrayOf(id),
                        null
                    )
                    phoneCursor?.use { pc ->
                        while (pc.moveToNext()) {
                            val number = pc.getString(
                                pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                            )
                            phoneNumbers.add(number)
                        }
                    }
                }
                contacts.add(Contact(id, name, phoneNumbers))
            }
        }
        return contacts
    }
}