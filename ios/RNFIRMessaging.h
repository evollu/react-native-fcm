
#import <UIKit/UIKit.h>

@import FirebaseAnalytics;
@import FirebaseInstanceID;
@import FirebaseMessaging;

#import "RCTBridgeModule.h"


extern NSString *const FCMNotificationReceived;

@interface RNFIRMessaging : NSObject <RCTBridgeModule>

@property (nonatomic, assign) bool connectedToFCM;

- (void)connectToFCM;

@end
