package com.bilalazzam.mycontacts

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.bilalazzam.contacts_provider.ContactsProvider
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                contactsProvider = ContactsProvider(this)
            )
        }
    }

    companion object {
        private const val SYNC_WORK_NAME = "contacts_sync_work"

        fun enqueueOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<ContactsSyncWorker>()
                .setConstraints(constraints)
                .build()

            Log.d("ContactsApp", "🕒 Enqueuing one-time sync request...")

            WorkManager.getInstance(context).beginUniqueWork(
                SYNC_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            ).enqueue()
        }
    }
}
