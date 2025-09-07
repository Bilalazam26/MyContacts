package com.bilalazzam.mycontacts

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController() = ComposeUIViewController { App(contactsProvider = ContactsProvider()) }