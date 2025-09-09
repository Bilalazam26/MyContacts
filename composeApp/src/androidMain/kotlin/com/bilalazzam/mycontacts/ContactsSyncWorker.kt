package com.bilalazzam.mycontacts

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bilalazzam.contacts_provider.ContactsProvider

class ContactsSyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            Log.d("ContactsSyncWorker", "Worker started")

            val contacts = ContactsProvider(applicationContext).getAllContacts()
            Log.d("ContactsSyncWorker", " Synced contacts count: ${contacts.size}")

            if (contacts.isNotEmpty()) {
                Log.d("ContactsSyncWorker", "Sync success with ${contacts.size} contacts")
                Result.success()
            } else {
                Log.d("ContactsSyncWorker", "No contacts found but sync finished")
                Result.success()
            }
        } catch (t: Throwable) {
            Log.e("ContactsSyncWorker", "Sync failed: ${t.message}", t)
            Result.retry()
        }
    }
}



