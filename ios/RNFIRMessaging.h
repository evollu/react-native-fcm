
#import <UIKit/UIKit.h>

@import FirebaseInstanceID;

#import <React/RCTBridgeModule.h>


extern NSString *const FCMNotificationReceived;

@interface RNFIRMessaging : NSObject <RCTBridgeModule>

@property (nonatomic, assign) bool connectedToFCM;

@end
