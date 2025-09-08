import SwiftUI
import BackgroundTasks

@main
struct iOSApp: App {
    init() {
        BGTaskScheduler.shared.register(forTaskWithIdentifier: "com.bilalazzam.mycontacts.contactsSync", using: nil) { task in
            self.handleContactsSync(task: task as! BGAppRefreshTask)
        }
        scheduleAppRefresh()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }

    private func scheduleAppRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: "com.bilalazzam.mycontacts.contactsSync")
        request.earliestBeginDate = Date(timeIntervalSinceNow: 15 * 60)
        do {
            try BGTaskScheduler.shared.submit(request)
        } catch {
            print("Could not schedule app refresh: \(error)")
        }
    }

    private func handleContactsSync(task: BGAppRefreshTask) {
        scheduleAppRefresh()

        let queue = OperationQueue()
        task.expirationHandler = {
            queue.cancelAllOperations()
        }

        queue.addOperation {
            print("Running iOS background contacts sync")
        }

        queue.addOperation {
            task.setTaskCompleted(success: true)
        }
    }
}