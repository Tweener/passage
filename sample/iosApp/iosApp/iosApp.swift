//
//  iosAppApp.swift
//  iosApp
//
//  Created by Vivien Mahé on 27/11/2024.
//

import SwiftUI

@main
struct iosApp: App {

    @UIApplicationDelegateAdaptor(AppDelegate.self)
    var appDelegate: AppDelegate

    var body: some Scene {
        WindowGroup {
            ContentView().onOpenURL(perform: { url in
                appDelegate.handleIncomingURL(url)
            })
        }
    }
}
