//
//  AppDelegate.swift
//  iosApp
//
//  Created by Vivien Mahé on 27/11/2024.
//

import SwiftUI
import GoogleSignIn
import composeApp

@MainActor
class AppDelegate : NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {
    
    nonisolated func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
        completionHandler([.banner, .badge, .sound])
    }
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        return GIDSignIn.sharedInstance.handle(url)
    }
    
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        return true
    }
    
    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        if userActivity.activityType == NSUserActivityTypeBrowsingWeb,
           let url = userActivity.webpageURL {
            handleIncomingURL(url)
            return true
        }
        
        print("No valid URL in user activity.")
        return false
    }
    
    func handleIncomingURL(_ url: URL) {
        print("handleIncomingURL", url)
        
        // Check if the URL is handled by Google Sign In
        if (GIDSignIn.sharedInstance.handle(url)) {
            print("Handled by GIDSignIn")
        }

        // Then check if the URL is handled by Passage (Firebase email action links)
        else if (PassageHelper().handle(url: url.absoluteString)) {
            print("Handled by Passage")
        }
    }
}
