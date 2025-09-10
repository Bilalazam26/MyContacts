package com.bilalazzam.mycontacts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.cachedIn
import androidx.paging.map
import com.bilalazzam.contacts_provider.Contact
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ContactsViewModel(
    private val contactsRepository: ContactsRepository,
    private val controller: PermissionsController
) : ViewModel() {

    var permissionState by mutableStateOf(PermissionState.NotDetermined)
        private set

    var isLoading by mutableStateOf(false)
        private set

    private var denyCount = 0

    init {
        checkPermission()
    }

    private fun checkPermission() {
        viewModelScope.launch {
            val currentPermissionState = controller.getPermissionState(Permission.CONTACTS)
            permissionState = currentPermissionState
        }
    }

    fun loadContacts(): Flow<PagingData<ContactsUiState>> {
        return createPagingFlow(
            pagingSourceFactory = { createContactsPagingSource() },
            mapper = Contact::toUiState
        ).cachedIn(viewModelScope)
    }

    private fun createContactsPagingSource(): PagingSource<Int, Contact> {
        return BasePagingSource { page, pageSize ->
            contactsRepository.getUserContacts(
                pageNumber = page,
                pageSize = pageSize
            )
        }
    }

    private fun <T : Any, R : Any> createPagingFlow(
        pagingSourceFactory: () -> PagingSource<Int, T>, mapper: (T) -> R,
    ): Flow<PagingData<R>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = 1
            ),
            pagingSourceFactory = pagingSourceFactory
        ).flow.map { pagingData ->
            pagingData.map(mapper)
        }
    }

    companion object {
        const val PAGE_SIZE = 20
    }

    fun requestContactsPermission() {
        viewModelScope.launch {
            try {
                controller.providePermission(Permission.CONTACTS)
                permissionState = PermissionState.Granted
            } catch (_: DeniedAlwaysException) {
                permissionState = PermissionState.DeniedAlways
            } catch (_: DeniedException) {
                denyCount++
                permissionState = if (denyCount > 3) {
                    PermissionState.DeniedAlways
                } else {
                    PermissionState.Denied
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}