package com.bilalazzam.mycontacts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bilalazzam.contacts_provider.Contact
import com.bilalazzam.contacts_provider.ContactsProvider
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsViewModel(
    private val contactsProvider: ContactsProvider,
    private val controller: PermissionsController
) : ViewModel() {

    var permissionState by mutableStateOf(PermissionState.NotDetermined)
        private set

    var contacts by mutableStateOf<List<Contact>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    private var denyCount = 0

    init {
        checkAndLoadContacts()
    }

    private fun checkAndLoadContacts() {
        viewModelScope.launch {
            val currentPermissionState = controller.getPermissionState(Permission.CONTACTS)
            permissionState = currentPermissionState

            if (permissionState == PermissionState.Granted) {
                getAllContacts()
            }
        }
    }

    fun getAllContacts() {
        viewModelScope.launch {
            isLoading = true
            try {
                val allContacts = withContext(Dispatchers.IO) {
                    contactsProvider.getAllContacts()
                }
                contacts = allContacts.filter { it.hasPhoneNumbers }
            } catch (e: Exception) {
            } finally {
                isLoading = false
            }
        }
    }

    fun requestContactsPermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.CONTACTS)
                permissionState = PermissionState.Granted
                getAllContacts()
            } catch (e: DeniedAlwaysException) {
                permissionState = PermissionState.DeniedAlways
            } catch (e: DeniedException) {
                denyCount++
                permissionState = if (denyCount > 3) {
                    PermissionState.DeniedAlways
                } else {
                    PermissionState.Denied
                }
            } catch (e: Exception) {
            }
        }
    }


}