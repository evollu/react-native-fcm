
#import <UIKit/UIKit.h>

#import <FirebaseCore/FIRApp.h>

#import <React/RCTBridgeModule.h>


extern NSString *const FCMNotificationReceived;

@interface RNFIRMessaging : NSObject <RCTBridgeModule>

@property (nonatomic, assign) bool connectedToFCM;

@end

