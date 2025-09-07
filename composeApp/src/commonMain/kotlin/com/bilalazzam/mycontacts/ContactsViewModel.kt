package com.bilalazzam.mycontacts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val contactsProvider: ContactsProvider,
    private val controller: PermissionsController
) : ViewModel() {

    var permissionState by mutableStateOf(PermissionState.NotDetermined)
        private set

    var contacts by mutableStateOf<List<Contact>>(emptyList())
        private set

    init {
        getAllContacts()
    }

    private fun refreshPermissionState() {
        viewModelScope.launch {
            val current = controller.getPermissionState(Permission.CONTACTS)
            println("TAG, refreshPermissionState: $current")
            permissionState = current

            if (permissionState == PermissionState.Granted) {
                getAllContacts()
            } else {
                provideOrRequestContactsAccessPermission()
            }
        }
    }

    fun getAllContacts() {
        viewModelScope.launch {
            contacts = contactsProvider.getAllContacts()
            println("TAG , contacts: $contacts")
        }
    }

    private fun provideOrRequestContactsAccessPermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.CONTACTS)
                permissionState = PermissionState.Granted
            } catch (e: DeniedAlwaysException) {
                println("TAG, provideOrRequestContactsAccessPermission: DeniedAlwaysException")
            } catch (e: DeniedException) {
                println("TAG, provideOrRequestContactsAccessPermission: DeniedException")
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                refreshPermissionState()
            }
        }
    }
}