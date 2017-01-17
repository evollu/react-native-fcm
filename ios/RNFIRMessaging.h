
#import <UIKit/UIKit.h>

#import <FirebaseCore/FirebaseCore.h>
#import <FirebaseCore/FIRConfiguration.h>
#import <FirebaseCore/FIRApp.h>
#import <FirebaseCore/FIRAnalyticsConfiguration.h>
#import <FirebaseCore/FIROptions.h>

#import <React/RCTBridgeModule.h>


extern NSString *const FCMNotificationReceived;

@interface RNFIRMessaging : NSObject <RCTBridgeModule>

@property (nonatomic, assign) bool connectedToFCM;

@end

