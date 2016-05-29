#import "FCMModule.h"

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


@implementation FCMModule

NSString* registrationToken;

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
  
  [FIRApp configure];

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
  
  //UIApplication *app = RCTSharedApplication();
  
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

//- (void)handleRemoteNotificationsRegistered:(NSNotification *)notification
//{
//  if([notification.userInfo objectForKey:@"deviceToken"] != nil){
//    NSData* deviceToken = [notification.userInfo objectForKey:@"deviceToken"];
//    __weak typeof(self) weakSelf = self;;
//    
//    NSDictionary *registrationOptions = @{kGGLInstanceIDRegisterAPNSOption:deviceToken,
//                                          kGGLInstanceIDAPNSServerTypeSandboxOption:@YES};
//    
//    NSString* gcmSenderID = [[[GGLContext sharedInstance] configuration] gcmSenderID];
//    
//    [[GGLInstanceID sharedInstance] tokenWithAuthorizedEntity:gcmSenderID scope:kGGLInstanceIDScopeGCM options:registrationOptions
//                                                      handler:^(NSString *token, NSError *error){
//                                                        if (token != nil) {
//                                                          NSLog(@"Registration Token: %@", token);
//                                                          
//                                                          weakSelf.connectedToGCM = YES;
//                                                          registrationToken = token;
//                                                          
//                                                          NSDictionary *userInfo = @{@"registrationToken":token};
//                                                          [_bridge.eventDispatcher sendDeviceEventWithName:GCMRemoteNotificationRegistered
//                                                                                                      body:userInfo];
//                                                        } else {
//                                                          NSLog(@"Registration to GCM failed with error: %@", error.localizedDescription);
//                                                          NSDictionary *userInfo = @{@"error":error.localizedDescription};
//                                                          [_bridge.eventDispatcher sendDeviceEventWithName:GCMRemoteNotificationRegistered
//                                                                                                      body:userInfo];
//                                                        }
//                                                      }];
//  } else {
//    [_bridge.eventDispatcher sendDeviceEventWithName:GCMRemoteNotificationRegistered
//                                                body:notification.userInfo];
//  }
//  
//}

//-(void)onTokenRefresh {
//  [self requestPermissions];
//}

@end
