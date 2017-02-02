
#import <UIKit/UIKit.h>

#import <FirebaseCore/FIRApp.h>

#import <React/RCTBridgeModule.h>

@import UserNotifications;

@interface RNFIRMessaging : NSObject <RCTBridgeModule>

typedef void (^RCTRemoteNotificationCallback)(UIBackgroundFetchResult result);
typedef void (^RCTWillPresentNotificationCallback)(UNNotificationPresentationOptions result);
typedef void (^RCTNotificationResponseCallback)();

@property (nonatomic, assign) bool connectedToFCM;

#if !TARGET_OS_TV
+ (void)didReceiveRemoteNotification:(nonnull NSDictionary *)userInfo fetchCompletionHandler:(nonnull RCTRemoteNotificationCallback)completionHandler;
+ (void)didReceiveLocalNotification:(nonnull UILocalNotification *)notification;
+ (void)didReceiveNotificationResponse:(nonnull UNNotificationResponse *)response withCompletionHandler:(nonnull RCTNotificationResponseCallback)completionHandler;
+ (void)willPresentNotification:(nonnull UNNotification *)notification withCompletionHandler:(nonnull RCTWillPresentNotificationCallback)completionHandler;
#endif

@end

