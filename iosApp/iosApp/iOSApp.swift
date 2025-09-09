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
            print("iOS: Background task scheduled successfully")
        } catch {
            print("iOS: Could not schedule app refresh: \(error)")
        }
    }

    private func handleContactsSync(task: BGAppRefreshTask) {
        print("iOS: Background sync task started")
                scheduleAppRefresh()
        
        let queue = OperationQueue()
        task.expirationHandler = {
            print("iOS: Background task expired")
            queue.cancelAllOperations()
        }

        queue.addOperation {
            print("iOS: Running background contacts sync")
            
            let syncManager = IOSContactsSyncManager()
            syncManager.handleBackgroundSync(task: task)
        }
    }
}