
#import <UIKit/UIKit.h>

@import Firebase;
@import FirebaseMessaging;

#import "RCTBridgeModule.h"


extern NSString *const FCMNotificationReceived;

@interface RNFIRMessaging : NSObject <RCTBridgeModule>

@property (nonatomic, assign) bool connectedToFCM;

@end
