package com.bilalazzam.contacts_provider


expect class ContactsCache {
    fun getContacts(): List<Contact>
    fun saveContacts(contacts: List<Contact>)
}
