#import "RNFIRMessaging.h"

#import "RCTBridge.h"
#import "RCTConvert.h"
#import "RCTEventDispatcher.h"
#import "RCTUtils.h"

#if __IPHONE_OS_VERSION_MIN_REQUIRED < __IPHONE_8_0

#define UIUserNotificationTypeAlert UIRemoteNotificationTypeAlert
#define UIUserNotificationTypeBadge UIRemoteNotificationTypeBadge
#define UIUserNotificationTypeSound UIRemoteNotificationTypeSound
#define UIUserNotificationTypeNone  UIRemoteNotificationTypeNone
#define UIUserNotificationType      UIRemoteNotificationType

#endif

NSString *const FCMNotificationReceived = @"FCMNotificationReceived";


@implementation RNFIRMessaging

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;

- (NSDictionary<NSString *, id> *)constantsToExport
{
  NSDictionary<NSString *, id> *initialNotification =
  [_bridge.launchOptions[UIApplicationLaunchOptionsRemoteNotificationKey] copy];
  return @{@"initialNotification": RCTNullIfNil(initialNotification)};
}

- (void)dealloc
{
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)setBridge:(RCTBridge *)bridge
{
  _bridge = bridge;

  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(handleRemoteNotificationReceived:)
                                               name:FCMNotificationReceived
                                             object:nil];
  
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(disconnectFCM)
                                               name:UIApplicationDidEnterBackgroundNotification
                                             object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(connectToFCM)
                                               name:UIApplicationDidBecomeActiveNotification
                                             object:nil];
  [[NSNotificationCenter defaultCenter]
   addObserver:self selector:@selector(onTokenRefresh)
   name:kFIRInstanceIDTokenRefreshNotification object:nil];

}

- (void)connectToFCM
{
  [[FIRMessaging messaging] connectWithCompletion:^(NSError * _Nullable error) {
    if (error != nil) {
      NSLog(@"Unable to connect to FCM. %@", error);
    } else {
      NSLog(@"Connected to FCM.");
    }
  }];
}

- (void)disconnectFCM
{
  [[FIRMessaging messaging] disconnect];
  NSLog(@"Disconnected from FCM");
}

RCT_REMAP_METHOD(getFCMToken,
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
  resolve([[FIRInstanceID instanceID] token]);
}

- (void) onTokenRefresh
{
  NSDictionary *info = @{@"token":[[FIRInstanceID instanceID] token]};
  [_bridge.eventDispatcher sendDeviceEventWithName:@"FCMTokenRefreshed"
                                                   body:info];
}

RCT_EXPORT_METHOD(requestPermissions)
{
  if (RCTRunningInAppExtension()) {
    return;
  }
  
  if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_7_1) {
    // iOS 7.1 or earlier
    UIRemoteNotificationType allNotificationTypes =
    (UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeBadge);
    [[UIApplication sharedApplication] registerForRemoteNotificationTypes:allNotificationTypes];
  } else {
    // iOS 8 or later
    // [END_EXCLUDE]
    UIUserNotificationType allNotificationTypes =
    (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge);
    UIUserNotificationSettings *settings =
    [UIUserNotificationSettings settingsForTypes:allNotificationTypes categories:nil];
    [[UIApplication sharedApplication] registerUserNotificationSettings:settings];
    [[UIApplication sharedApplication] registerForRemoteNotifications];
  }
  
}

- (void)handleRemoteNotificationReceived:(NSNotification *)notification
{
  [_bridge.eventDispatcher sendDeviceEventWithName:FCMNotificationReceived
                                              body:notification.userInfo];
}

@end
