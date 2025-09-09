import SwiftUI
import BackgroundTasks

@main
struct iOSApp: App {
    init() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.bilalazzam.mycontacts.contactsSync", using: nil) { task in
            self.handleContactsSync(task: task as! BGAppRefreshTask)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView(onManualSync: {
                scheduleOneTimeBackgroundSync()
            })
        }
    }

    private func scheduleOneTimeBackgroundSync() {
        let request = BGAppRefreshTaskRequest(identifier: "com.bilalazzam.mycontacts.contactsSync")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 5)

        do {
            try BGTaskScheduler.shared.submit(request)
            print("iOS: One-time background sync scheduled")
        } catch {
            print("iOS: Failed to schedule background sync: \(error)")
        }
    }

    private func handleContactsSync(task: BGAppRefreshTask) {
        print("iOS: Background sync task started")

        let queue = OperationQueue()
        task.expirationHandler = {
            print("iOS: Background task expired")
            queue.cancelAllOperations()
        }

        queue.addOperation {
            print("iOS: Running one-time background contacts sync")
            let syncManager = IOSContactsSyncManager()
            syncManager.handleBackgroundSync(task: task)
        }
    }
}