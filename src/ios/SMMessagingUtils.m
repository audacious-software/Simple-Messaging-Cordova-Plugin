@import UserNotifications;

#import "SMMessagingUtils.h"

@interface NSData(HexString)
- (NSString *) hexString;
@end

@implementation AppDelegate(SMMessagingUtilsDelegate)

-(void)application:(UIApplication *)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData *)deviceToken {
    deviceToken = [NSMutableData dataWithData:deviceToken];

    NSUserDefaults * defaults = [NSUserDefaults standardUserDefaults];
    [defaults setValue:[deviceToken hexString] forKey:@"SMMessagingUtils.deviceToken"];
    [defaults synchronize];
}

/*
 Add to Swift app delegate if app is in Swift:
 
  func application(_ application: UIApplication, didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
    let token = deviceToken.map { String(format: "%02.2hhx", $0) }.joined()

    UserDefaults.standard.set(token, forKey: "SMMessagingUtils.deviceToken")
        
    UserDefaults.standard.synchronize()
  }
 */

@end

@implementation SMMessagingUtils

- (void) fetchDeviceTokenAndTransmit:(CDVInvokedUrlCommand *) command {
    NSLog(@"SMMessagingUtils.fetchDeviceTokenAndTransmit: %@", command);

    NSTimeInterval delayInSeconds = 1.0;

    dispatch_time_t popTime = dispatch_time(DISPATCH_TIME_NOW, (int64_t)(delayInSeconds * NSEC_PER_SEC));

    dispatch_after(popTime, dispatch_get_main_queue(), ^(void){
        NSLog(@"SMMessagingUtils.fetchDeviceTokenAndTransmit: register[1]");
    
        NSString * endpointUrl = [command argumentAtIndex:0];
        NSString * username = [command argumentAtIndex:1];

        NSUserDefaults * defaults = [NSUserDefaults standardUserDefaults];

        NSString * deviceToken = [defaults stringForKey:@"SMMessagingUtils.deviceToken"];

        NSLog(@"SMMessagingUtils.fetchDeviceTokenAndTransmit: register[2]: %@, %@, %@", endpointUrl, username, deviceToken);

        if (deviceToken != nil) {
            NSURL * url = [NSURL URLWithString:endpointUrl];

            NSMutableURLRequest *request = [[NSMutableURLRequest alloc] initWithURL:url];

            request.HTTPMethod = @"POST";

            NSString * formString = [NSString stringWithFormat:@"platform=ios&identifier=%@&token=%@", username, deviceToken];

            NSData * requestData = [formString dataUsingEncoding:NSUTF8StringEncoding];

            [request setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
            [request setValue:[NSString stringWithFormat:@"%lul", (unsigned long)[requestData length]] forHTTPHeaderField:@"Content-Length"];
            [request setHTTPBody: requestData];

            NSURLSessionConfiguration *defaultSessionConfiguration = [NSURLSessionConfiguration defaultSessionConfiguration];
            NSURLSession *defaultSession = [NSURLSession sessionWithConfiguration:defaultSessionConfiguration];

            NSURLSessionDataTask *dataTask = [defaultSession dataTaskWithRequest:request completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
                NSLog(@"SMMessagingUtils.fetchDeviceTokenAndTransmit: register[3]: %@, %d,", response, error);

                NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *) response;

                NSLog(@"SMMessagingUtils.fetchDeviceTokenAndTransmit: register[4]: %@", @(response.statusCode));

                if (httpResponse.statusCode < 200 || httpResponse.statusCode >= 300) {
                    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                                      messageAsString:@"Token successfully registered with the server."];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                } else {
                    NSString * message = [NSString stringWithFormat:@"Unexpected code: %ld", (long) httpResponse.statusCode];

                    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                                      messageAsString:message];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                }
            }];

            [dataTask resume];
        }
    });

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) fetchPermissions:(CDVInvokedUrlCommand *) command {
    NSLog(@"SMMessagingUtils.fetchPermissions: %@", command);

    dispatch_async(dispatch_get_main_queue(), ^{
        [[UNUserNotificationCenter currentNotificationCenter] getNotificationSettingsWithCompletionHandler:^(UNNotificationSettings * _Nonnull settings) {
            switch (settings.authorizationStatus) {
                case UNAuthorizationStatusNotDetermined: {
                    [[UNUserNotificationCenter currentNotificationCenter] requestAuthorizationWithOptions:(UNAuthorizationOptionAlert|UNAuthorizationOptionSound)
                                                                                        completionHandler:^(BOOL granted, NSError * _Nullable error) {
                        if (error == nil) {
                            if (granted) {
                                dispatch_async(dispatch_get_main_queue(), ^{
                                    [[UIApplication sharedApplication] registerForRemoteNotifications];

                                    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:1];
                                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                                });

                                return;
                            }
                        }
                    }];
                    break;
                }
                case UNAuthorizationStatusDenied: {
                    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:0];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                }
                case UNAuthorizationStatusAuthorized: {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[UIApplication sharedApplication] registerForRemoteNotifications];

                        CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:1];
                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                    });

                    break;
                }
                case UNAuthorizationStatusProvisional: {
                    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:1];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

                    break;
                }
                case UNAuthorizationStatusEphemeral: {
                    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt:1];
                    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];

                    break;
                }
            }
        }];
    });

    CDVPluginResult* pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_NO_RESULT];
    [pluginResult setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end

