package com.bilalazzam.contacts_provider

import platform.Foundation.NSUserDefaults
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class ContactsCache {
    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val json = Json { ignoreUnknownKeys = true }

    actual fun getContacts(): List<Contact> {
        val jsonString = userDefaults.stringForKey("contacts_list")
        return jsonString?.let {
            json.decodeFromString(it)
        } ?: emptyList()
    }

    actual fun saveContacts(contacts: List<Contact>) {
        val jsonString = json.encodeToString(contacts)
        userDefaults.setObject(jsonString, "contacts_list")
    }
}
