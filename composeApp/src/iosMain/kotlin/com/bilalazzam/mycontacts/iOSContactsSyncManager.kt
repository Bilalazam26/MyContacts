package com.bilalazzam.mycontacts

import com.bilalazzam.contacts_provider.ContactsProvider
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate

class iOSContactsSyncManager(private val contactsProvider: ContactsProvider) : ContactsSyncManager {
    override fun enqueueSync() {
        scheduleAppRefreshTask()
    }
}
