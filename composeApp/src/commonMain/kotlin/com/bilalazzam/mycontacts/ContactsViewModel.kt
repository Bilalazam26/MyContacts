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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsViewModel(
    private val contactsProvider: ContactsProvider,
    private val controller: PermissionsController,
    private val syncManager: ContactsSyncManager

) : ViewModel() {

    var permissionState by mutableStateOf(PermissionState.NotDetermined)
        private set

    var contacts by mutableStateOf<List<Contact>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var workManagerStatus by mutableStateOf(WorkManagerStatus())
        private set

    var cacheInfo by mutableStateOf("No cached data / لا توجد بيانات مخزنة")
        private set

    private var denyCount = 0
    private val contactsCache = ContactsCache()
    private val workManagerTracker = WorkManagerStatusTracker()

    init {
        checkAndLoadContacts()
    }

    private fun checkAndLoadContacts() {
        viewModelScope.launch {
            val currentPermissionState = controller.getPermissionState(Permission.CONTACTS)
            permissionState = currentPermissionState

            if (permissionState == PermissionState.Granted) {
                // Check cache first
                if (contactsCache.hasCachedContacts()) {
                    val cachedContacts = contactsCache.getCachedContacts()
                    if (cachedContacts != null) {
                        contacts = cachedContacts.filter { it.hasPhoneNumbers }
                        cacheInfo = contactsCache.getCacheInfo()
                    }
                }
                getAllContacts()
            }
            
            // Update work manager status
            workManagerStatus = workManagerTracker.getStatus()
        }
    }

    fun getAllContacts() {
        viewModelScope.launch {
            isLoading = true
            workManagerTracker.markAsRunning()
            workManagerStatus = workManagerTracker.getStatus()
            
            try {
                  val allContacts=  contactsProvider.getAllContacts()

                val filteredContacts = allContacts.filter { it.hasPhoneNumbers }
                contacts = filteredContacts
                
                // Save to cache
                contactsCache.saveContacts(filteredContacts)
                cacheInfo = contactsCache.getCacheInfo()
                
                workManagerTracker.markAsCompleted("success")
            } catch (e: Exception) {
                workManagerTracker.markAsCompleted("failure")
            } finally {
                isLoading = false
                workManagerStatus = workManagerTracker.getStatus()
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

    fun reSyncContacts() {
        viewModelScope.launch {
            workManagerTracker.markAsScheduled()
            workManagerStatus = workManagerTracker.getStatus()
            
            syncManager.enqueueSync()
            getAllContacts()
        }
    }
    
    fun clearCache() {
        contactsCache.clearCache()
        cacheInfo = contactsCache.getCacheInfo()
        contacts = emptyList()
    }
    
    fun getWorkManagerDetailedInfo(): String {
        return workManagerTracker.getStatus().getDetailedInfo()
    }
    
    fun testWorkManager() {
        viewModelScope.launch {
            workManagerTracker.markAsScheduled()
            workManagerStatus = workManagerTracker.getStatus()
            
            // Simulate work manager execution
            workManagerTracker.markAsRunning()
            workManagerStatus = workManagerTracker.getStatus()
            
            // Simulate some work
            kotlinx.coroutines.delay(2000)
            
            // Complete the work
            workManagerTracker.markAsCompleted("success")
            workManagerStatus = workManagerTracker.getStatus()
            
            // Refresh contacts to show the work manager worked
            getAllContacts()
        }
    }
}