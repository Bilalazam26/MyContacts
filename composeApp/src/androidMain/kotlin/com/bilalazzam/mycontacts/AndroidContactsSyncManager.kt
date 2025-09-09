package com.bilalazzam.mycontacts

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

class AndroidContactsSyncManager(private val context: Context) : ContactsSyncManager {
    override fun enqueueSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = OneTimeWorkRequestBuilder<ContactsSyncWorker>()
            .setConstraints(constraints)
            .build()

        Log.d("ContactsApp", "Enqueuing Re-Sync request")

        WorkManager.getInstance(context).beginUniqueWork(
            "contacts_sync_work",
            ExistingWorkPolicy.REPLACE,
            syncRequest
        ).enqueue()
    }
}
