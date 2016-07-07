
#import <UIKit/UIKit.h>

#import "Firebase.h"

#import "RCTBridgeModule.h"


extern NSString *const FCMNotificationReceived;

@interface RNFIRMessaging : NSObject <RCTBridgeModule>

@property (nonatomic, assign) bool connectedToFCM;

@end
