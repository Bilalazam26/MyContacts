package com.bilalazzam.mycontacts

import com.bilalazzam.contacts_provider.Contact
import com.bilalazzam.contacts_provider.ContactAvatar
import com.bilalazzam.contacts_provider.ContactsProvider
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.IO

class ContactsRepository(
    private val contactsProvider: ContactsProvider
) {
    companion object {
        private const val MAX_PAGES = 20
        private const val TOTAL_FAKE_CONTACTS = 150
        private const val NETWORK_DELAY_MS = 300L
    }

    suspend fun getUserContacts(pageNumber: Int, pageSize: Int): PagedData<Contact> {
        return withContext(Dispatchers.IO) {
            try {
                if (pageNumber < 1 || pageNumber > MAX_PAGES) {
                    Napier.w("Invalid page number: $pageNumber", tag = "ContactsRepository")
                    return@withContext PagedData(
                        data = emptyList(),
                        totalItems = 0,
                        isLastPage = true
                    )
                }

                if (pageSize <= 0 || pageSize > 100) {
                    Napier.w("Invalid page size: $pageSize", tag = "ContactsRepository")
                    return@withContext PagedData(
                        data = emptyList(),
                        totalItems = 0,
                        isLastPage = true
                    )
                }

                Napier.i("getUserContacts page=$pageNumber, size=$pageSize (FAKE DATA)", tag = "ContactsRepository")
                
                delay(NETWORK_DELAY_MS)

                val fakeContacts = generateFakeContacts(TOTAL_FAKE_CONTACTS)
                val totalItems = fakeContacts.size

                if (totalItems == 0) {
                    return@withContext PagedData(
                        data = emptyList(),
                        totalItems = 0,
                        isLastPage = true
                    )
                }

                val startIndex = (pageNumber - 1) * pageSize

                if (startIndex >= totalItems) {
                    return@withContext PagedData(
                        data = emptyList(),
                        totalItems = totalItems,
                        isLastPage = true
                    )
                }

                val endIndex = minOf(startIndex + pageSize, totalItems)
                val pageData = fakeContacts.subList(startIndex, endIndex)
                val isLastPage = endIndex >= totalItems

                Napier.i("Page $pageNumber: ${pageData.size} fake items (startIndex=$startIndex, endIndex=$endIndex, total=$totalItems, isLast=$isLastPage)", tag = "ContactsRepository")

                PagedData(
                    data = pageData,
                    totalItems = totalItems,
                    isLastPage = isLastPage
                )
            } catch (e: Exception) {
                Napier.e("Error generating fake contacts: ${e.message}", tag = "ContactsRepository", throwable = e)
                PagedData(
                    data = emptyList(),
                    totalItems = 0,
                    isLastPage = true
                )
            }
        }
    }

    private fun generateFakeContacts(count: Int): List<Contact> {
        return (1..count).map { index ->
            // Generate more realistic fake data
            val firstName = when (index % 10) {
                1 -> "Emma"
                2 -> "Liam"
                3 -> "Olivia"
                4 -> "Noah"
                5 -> "Ava"
                6 -> "William"
                7 -> "Sophia"
                8 -> "James"
                9 -> "Isabella"
                else -> "Benjamin"
            }
            
            val lastName = when (index % 8) {
                1 -> "Smith"
                2 -> "Johnson"
                3 -> "Williams"
                4 -> "Brown"
                5 -> "Jones"
                6 -> "Garcia"
                7 -> "Miller"
                else -> "Davis"
            }
            
            Contact(
                id = "fake_$index",
                firstName = firstName,
                lastName = lastName,
                phoneNumbers = listOf(
                    "(555) ${(index % 999).toString().padStart(3, '0')}-${(index * 2 % 9999).toString().padStart(4, '0')}",
                    "+1 555 ${(index % 999).toString().padStart(3, '0')} ${(index * 3 % 9999).toString().padStart(4, '0')}"
                ),
                avatar = ContactAvatar.None
            )
        }
    }
}