
#import <UIKit/UIKit.h>

#if __has_include(<Firebase.h>)
#import <FirebaseCore/FirebaseCore.h>
#import <FirebaseMessaging/FirebaseMessaging.h>
#import <FirebaseInstanceID/FirebaseInstanceID.h>
#else
@import Firebase;
#endif

#import <React/RCTEventEmitter.h>

@import UserNotifications;

@interface RNFIRMessaging : RCTEventEmitter <RCTBridgeModule, FIRMessagingDelegate>

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

