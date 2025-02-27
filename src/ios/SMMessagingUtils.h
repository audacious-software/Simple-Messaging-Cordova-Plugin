@import UIKit;

#import <Cordova/CDVPlugin.h>

#import "AppDelegate.h"

@interface AppDelegate(SMMessagingUtilsDelegate)
-(void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken;
@end

@interface SMMessagingUtils : CDVPlugin
- (void) fetchDeviceTokenAndTransmit:(CDVInvokedUrlCommand *)command;
@end

