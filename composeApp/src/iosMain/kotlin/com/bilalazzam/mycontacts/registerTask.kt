package com.bilalazzam.mycontacts

import com.bilalazzam.contacts_provider.ContactsProvider
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateByAddingTimeInterval

@OptIn(ExperimentalForeignApi::class)
fun registerTask(contactsProvider: ContactsProvider) {
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
        identifier = "contacts_sync_task",
        usingQueue = null
    ) { task ->
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val contacts = contactsProvider.getAllContacts()
                printLog("iOS App Refresh Task: Synced ${contacts.size} contacts")
                (task as? BGAppRefreshTask)?.setTaskCompletedWithSuccess(true)
            } catch (t: Throwable) {
                printLog("iOS App Refresh Task failed: ${t.message}")
                (task as? BGAppRefreshTask)?.setTaskCompletedWithSuccess(false)
            } finally {
                scheduleAppRefreshTask()
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun scheduleAppRefreshTask(earliestBeginDate: NSDate? = null) {
    val request = BGAppRefreshTaskRequest(identifier = "contacts_sync_task")
    request.earliestBeginDate = earliestBeginDate ?: NSDate().dateByAddingTimeInterval((Constants.TASK_TIMER_SECONDS * 60).toDouble())
    BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
}
