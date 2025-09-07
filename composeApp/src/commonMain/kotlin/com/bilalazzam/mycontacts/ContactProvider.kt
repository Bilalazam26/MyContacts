package com.bilalazzam.mycontacts

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class ContactsProvider {
    suspend fun getAllContacts(): List<Contact>
}