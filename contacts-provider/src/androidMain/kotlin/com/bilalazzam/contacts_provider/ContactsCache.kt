package com.bilalazzam.contacts_provider

import android.content.Context
import com.bilalazzam.contacts_provider.Contact
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class ContactsCache(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("contacts_cache", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    actual fun getContacts(): List<Contact> {
        val jsonString = sharedPreferences.getString("contacts_list", null)
        return jsonString?.let {
            json.decodeFromString(it)
        } ?: emptyList()
    }

    actual fun saveContacts(contacts: List<Contact>) {
        val jsonString = json.encodeToString(contacts)
        sharedPreferences.edit().putString("contacts_list", jsonString).apply()
    }
}
