package com.bilalazzam.mycontacts

import androidx.compose.ui.window.ComposeUIViewController
import com.bilalazzam.contacts_provider.ContactsProvider

fun MainViewController() = ComposeUIViewController { App(contactsProvider = ContactsProvider()) }