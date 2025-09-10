package com.bilalazzam.mycontacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bilalazzam.contacts_provider.ContactsProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val syncManager = AndroidContactsSyncManager(this)

        setContent {
            AndroidApp(
                contactsProvider = ContactsProvider(this),
                syncManager = syncManager
            )
        }
    }
}

@Composable
fun AndroidApp(contactsProvider: ContactsProvider, syncManager: ContactsSyncManager) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(Unit) {
        val workManager = WorkManager.getInstance(context)
        val liveData = workManager.getWorkInfosForUniqueWorkLiveData("contacts_sync_work")
        onDispose { /* observer removed inside App content */ }
    }

    AppWithObserver(contactsProvider, syncManager)
}

@Composable
private fun AppWithObserver(contactsProvider: ContactsProvider, syncManager: ContactsSyncManager) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val factory = dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory()
    val controller = androidx.compose.runtime.remember(factory) { factory.createPermissionsController() }
    dev.icerock.moko.permissions.compose.BindEffect(controller)

    val viewModel = androidx.lifecycle.viewmodel.compose.viewModel {
        ContactsViewModel(contactsProvider, controller, syncManager)
    }

    DisposableEffect(Unit) {
        val workManager = WorkManager.getInstance(context)
        val liveData = workManager.getWorkInfosForUniqueWorkLiveData("contacts_sync_work")
        val observer = Observer<List<WorkInfo>> { infos ->
            val state = infos.firstOrNull()?.state ?: return@Observer
            if (state == WorkInfo.State.SUCCEEDED ||
                state == WorkInfo.State.FAILED ||
                state == WorkInfo.State.CANCELLED) {
                viewModel.refreshContactsFromCache()
            }
        }
        liveData.observe(lifecycleOwner, observer)
        onDispose { liveData.removeObserver(observer) }
    }

    App(contactsProvider, syncManager)
}
