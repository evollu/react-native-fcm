#import "RNFIRMessaging.h"

#import "RCTBridge.h"
#import "RCTConvert.h"
#import "RCTEventDispatcher.h"
#import "RCTUtils.h"

@import UserNotifications;

#if __IPHONE_OS_VERSION_MIN_REQUIRED < __IPHONE_8_0

#define UIUserNotificationTypeAlert UIRemoteNotificationTypeAlert
#define UIUserNotificationTypeBadge UIRemoteNotificationTypeBadge
#define UIUserNotificationTypeSound UIRemoteNotificationTypeSound
#define UIUserNotificationTypeNone  UIRemoteNotificationTypeNone
#define UIUserNotificationType      UIRemoteNotificationType

#endif

NSString *const FCMNotificationReceived = @"FCMNotificationReceived";

@implementation RCTConvert (NSCalendarUnit)

+ (NSCalendarUnit *)NSCalendarUnit:(id)json
{
  NSString* key = [self NSString:json];
  if([key isEqualToString:@"minute"]){
    return NSCalendarUnitMinute;
  }
  if([key isEqualToString:@"second"]){
    return NSCalendarUnitSecond;
  }
  if([key isEqualToString:@"day"]){
    return NSCalendarUnitDay;
  }
  if([key isEqualToString:@"month"]){
    return NSCalendarUnitMonth;
  }
  if([key isEqualToString:@"week"]){
    return NSCalendarUnitWeekOfYear;
  }
  if([key isEqualToString:@"year"]){
    return NSCalendarUnitYear;
  }
  return 0;
}

@end

@implementation RCTConvert (UILocalNotification)

+ (UILocalNotification *)UILocalNotification:(id)json
{
  NSDictionary<NSString *, id> *details = [self NSDictionary:json];
  UILocalNotification *notification = [UILocalNotification new];
  notification.fireDate = [RCTConvert NSDate:details[@"fire_date"]] ?: [NSDate date];
  notification.alertTitle = [RCTConvert NSString:details[@"title"]];
  notification.alertBody = [RCTConvert NSString:details[@"body"]];
  notification.alertAction = [RCTConvert NSString:details[@"alert_action"]];
  notification.soundName = [RCTConvert NSString:details[@"sound"]] ?: UILocalNotificationDefaultSoundName;
  notification.userInfo = details;
  notification.category = [RCTConvert NSString:details[@"click_action"]];
  notification.repeatInterval = [RCTConvert NSCalendarUnit:details[@"repeat_interval"]];
  notification.applicationIconBadgeNumber = [RCTConvert NSInteger:details[@"badge"]];
  return notification;
}

@end

@implementation RNFIRMessaging

RCT_EXPORT_MODULE()

@synthesize bridge = _bridge;

- (void)dealloc
{
  [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)setBridge:(RCTBridge *)bridge
{
  _bridge = bridge;
  
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(handleNotificationReceived:)
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

  [[NSNotificationCenter defaultCenter]
     addObserver:self selector:@selector(sendDataMessageFailure:)
            name:FIRMessagingSendErrorNotification object:nil];

  [[NSNotificationCenter defaultCenter]
     addObserver:self selector:@selector(sendDataMessageSuccess:)
            name:FIRMessagingSendSuccessNotification object:nil];
  
  // For iOS 10 data message (sent via FCM)
  [[FIRMessaging messaging] setRemoteMessageDelegate:self];
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

RCT_EXPORT_METHOD(getInitialNotification:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
  NSDictionary *localUserInfo = _bridge.launchOptions[UIApplicationLaunchOptionsLocalNotificationKey];
  if(localUserInfo){
    resolve([localUserInfo copy]);
    return;
  }
  resolve([_bridge.launchOptions[UIApplicationLaunchOptionsRemoteNotificationKey] copy]);
}

RCT_EXPORT_METHOD(getFCMToken:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  resolve([[FIRInstanceID instanceID] token]);
}

RCT_EXPORT_METHOD(getScheduledLocalNotifications:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  NSMutableArray* list = [[NSMutableArray alloc] init];
  for(UILocalNotification * notif in [RCTSharedApplication() scheduledLocalNotifications]){
    NSString* interval;
    
    switch(notif.repeatInterval){
      case NSCalendarUnitMinute:
        interval = @"minute";
        break;
      case NSCalendarUnitSecond:
        interval = @"second";
        break;
      case NSCalendarUnitDay:
        interval = @"day";
        break;
      case NSCalendarUnitMonth:
        interval = @"month";
        break;
      case NSCalendarUnitWeekOfYear:
        interval = @"week";
        break;
      case NSCalendarUnitYear:
        interval = @"year";
        break;
    }
    NSMutableDictionary *formattedLocalNotification = [NSMutableDictionary dictionary];
    if (notif.fireDate) {
      NSDateFormatter *formatter = [NSDateFormatter new];
      [formatter setDateFormat:@"yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ"];
      NSString *fireDateString = [formatter stringFromDate:notif.fireDate];
      formattedLocalNotification[@"fire_date"] = fireDateString;
    }
    formattedLocalNotification[@"alert_action"] = RCTNullIfNil(notif.alertAction);
    formattedLocalNotification[@"body"] = RCTNullIfNil(notif.alertBody);
    formattedLocalNotification[@"title"] = RCTNullIfNil(notif.alertTitle);
    formattedLocalNotification[@"badge"] = @(notif.applicationIconBadgeNumber);
    formattedLocalNotification[@"click_action"] = RCTNullIfNil(notif.category);
    formattedLocalNotification[@"sound"] = RCTNullIfNil(notif.soundName);
    formattedLocalNotification[@"repeat_interval"] = RCTNullIfNil(interval);
    formattedLocalNotification[@"data"] = RCTNullIfNil(RCTJSONClean(notif.userInfo));
    [list addObject:formattedLocalNotification];
  }
  resolve(list);
}

- (void) onTokenRefresh
{
  [_bridge.eventDispatcher sendDeviceEventWithName:@"FCMTokenRefreshed" body:[[FIRInstanceID instanceID] token]];
}

RCT_EXPORT_METHOD(requestPermissions)
{
  if (RCTRunningInAppExtension()) {
    return;
  }
  if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_9_x_Max) {
    UIUserNotificationType allNotificationTypes =
    (UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge);
    UIApplication *app = RCTSharedApplication();
    if ([app respondsToSelector:@selector(registerUserNotificationSettings:)]) {
      //iOS 8 or later
      UIUserNotificationSettings *notificationSettings =
      [UIUserNotificationSettings settingsForTypes:(NSUInteger)allNotificationTypes categories:nil];
      [app registerUserNotificationSettings:notificationSettings];
    } else {
      //iOS 7 or below
      [app registerForRemoteNotificationTypes:(NSUInteger)allNotificationTypes];
    }
  } else {
    // iOS 10 or later
#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
    UNAuthorizationOptions authOptions =
    UNAuthorizationOptionAlert
    | UNAuthorizationOptionSound
    | UNAuthorizationOptionBadge;
    [[UNUserNotificationCenter currentNotificationCenter]
     requestAuthorizationWithOptions:authOptions
     completionHandler:^(BOOL granted, NSError * _Nullable error) {
     }
     ];
#endif
  }
  
  [[UIApplication sharedApplication] registerForRemoteNotifications];
}

#if defined(__IPHONE_10_0) && __IPHONE_OS_VERSION_MAX_ALLOWED >= __IPHONE_10_0
// Receive data message on iOS 10 devices.
- (void)applicationReceivedRemoteMessage:(FIRMessagingRemoteMessage *)remoteMessage {
  [_bridge.eventDispatcher sendDeviceEventWithName:FCMNotificationReceived body:[remoteMessage appData]];
}
#endif

RCT_EXPORT_METHOD(subscribeToTopic: (NSString*) topic)
{
  [[FIRMessaging messaging] subscribeToTopic:topic];
}

RCT_EXPORT_METHOD(unsubscribeFromTopic: (NSString*) topic)
{
  [[FIRMessaging messaging] unsubscribeFromTopic:topic];
}

RCT_EXPORT_METHOD(presentLocalNotification:(UILocalNotification *)notification)
{
  [RCTSharedApplication() presentLocalNotificationNow:notification];
}

RCT_EXPORT_METHOD(scheduleLocalNotification:(UILocalNotification *)notification)
{
  [RCTSharedApplication() scheduleLocalNotification:notification];
}

RCT_EXPORT_METHOD(cancelAllLocalNotifications)
{
  [RCTSharedApplication() cancelAllLocalNotifications];
}

RCT_EXPORT_METHOD(cancelLocalNotification:(NSString*) notificationId)
{
  for (UILocalNotification *notification in [UIApplication sharedApplication].scheduledLocalNotifications) {
    NSDictionary<NSString *, id> *notificationInfo = notification.userInfo;
    if([notificationId isEqualToString:[notificationInfo valueForKey:@"id"]]){
      [[UIApplication sharedApplication] cancelLocalNotification:notification];
    }
  }
}

RCT_EXPORT_METHOD(setBadgeNumber: (NSInteger*) number)
{
  [RCTSharedApplication() setApplicationIconBadgeNumber:number];
}

RCT_EXPORT_METHOD(getBadgeNumber: (RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
  resolve(@([RCTSharedApplication() applicationIconBadgeNumber]));
}

RCT_EXPORT_METHOD(send:(NSString*)senderId withPayload:(NSDictionary *)message)
{
  NSMutableDictionary * mMessage = [message mutableCopy];
  NSMutableDictionary * upstreamMessage = [[NSMutableDictionary alloc] init];
  for (NSString* key in mMessage) {
    upstreamMessage[key] = [NSString stringWithFormat:@"%@", [mMessage valueForKey:key]];
  }

  NSDictionary *imMessage = [NSDictionary dictionaryWithDictionary:upstreamMessage];

  int64_t ttl = 3600;
  NSString * receiver = [NSString stringWithFormat:@"%@@gcm.googleapis.com", senderId];

  NSUUID *uuid = [NSUUID UUID];
  NSString * messageID = [uuid UUIDString];

  [[FIRMessaging messaging]sendMessage:imMessage to:receiver withMessageID:messageID timeToLive:ttl];
}

- (void)handleNotificationReceived:(NSNotification *)notification
{
  if([notification.userInfo valueForKey:@"opened_from_tray"] == nil){
    NSMutableDictionary *data = [[NSMutableDictionary alloc]initWithDictionary: notification.userInfo];
    [data setValue:@(RCTSharedApplication().applicationState == UIApplicationStateInactive) forKey:@"opened_from_tray"];
    [_bridge.eventDispatcher sendDeviceEventWithName:FCMNotificationReceived body:data];
  }else{
    [_bridge.eventDispatcher sendDeviceEventWithName:FCMNotificationReceived body:notification.userInfo];
  }
  
}

- (void)sendDataMessageFailure:(NSNotification *)notification 
{
    NSString *messageID = (NSString *)notification.userInfo[@"messageID"];

    NSLog(@"sendDataMessageFailure: %@", messageID);
}

- (void)sendDataMessageSuccess:(NSNotification *)notification 
{
    NSString *messageID = (NSString *)notification.userInfo[@"messageID"];

    NSLog(@"sendDataMessageSuccess: %@", messageID);
}

@end
