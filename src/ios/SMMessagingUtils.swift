import UserNotifications

@objc(SMMessagingUtils)
class SMMessagingUtils : CDVPlugin {
    @objc(fetchPermissions:)
    func fetchPermissions(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(
            status: CDVCommandStatus_ERROR
        )

        pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: true)

        let center = UNUserNotificationCenter.current()

        do {
            try await center.requestAuthorization(options: [.alert, .sound, .badge])
        } catch {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: true)
        }

        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
    }

    @objc(fetchDeviceToken:)
    func fetchDeviceToken(command: CDVInvokedUrlCommand) {
        var pluginResult = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: true)

        let deviceTokenString = UserDefaults.standard.string(forKey:"simple_messaging_utils_device_token")

        if (deviceTokenString != nil) {
            pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: deviceTokenString)
        }

        self.commandDelegate!.send(
            pluginResult,
            callbackId: command.callbackId
        )
    }
}

extension AppDelegate {
    func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
        let deviceTokenString = deviceToken.hexString
        print(deviceTokenString)

        UserDefaults.standard.set(deviceTokenString, forKey:"simple_messaging_utils_device_token")
    }
}