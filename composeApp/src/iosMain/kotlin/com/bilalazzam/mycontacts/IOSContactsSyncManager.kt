package com.bilalazzam.mycontacts

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLog
import platform.BackgroundTasks.BGTaskScheduler
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGAppRefreshTask
import platform.Foundation.NSDate
import platform.Foundation.NSTimeInterval
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import com.bilalazzam.contacts_provider.ContactsProvider

class IOSContactsSyncManager : ContactsSyncManager {

    companion object {
        private const val TASK_IDENTIFIER = "com.bilalazzam.mycontacts.contactsSync"
    }

    override fun enqueueSync() {
        NSLog("IOSContactsSyncManager: Scheduling background sync")
        scheduleAppRefresh()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun scheduleAppRefresh() {
        val request = BGAppRefreshTaskRequest(TASK_IDENTIFIER)

        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
            NSLog("IOSContactsSyncManager: Background task scheduled successfully")
        } catch (e: Exception) {
            NSLog("IOSContactsSyncManager: Failed to schedule background task: ${e.message}")
        }
    }

    fun handleBackgroundSync(task: BGAppRefreshTask) {
        NSLog("IOSContactsSyncManager: Handling background sync task")

        scheduleAppRefresh()

        task.expirationHandler = {
            NSLog("IOSContactsSyncManager: Background task expired")
        }

        NSLog("IOSContactsSyncManager: Starting contacts sync...")

        runBlocking {
            try {
                val contactsProvider = ContactsProvider()
                val contacts = contactsProvider.getAllContacts()
                NSLog("IOSContactsSyncManager: Successfully synced ${contacts.size} contacts")
            } catch (e: Exception) {
                NSLog("IOSContactsSyncManager: Sync failed: ${e.message}")
            }
        }
        task.setTaskCompletedWithSuccess(true)
        NSLog("IOSContactsSyncManager: Background sync completed")
    }
}