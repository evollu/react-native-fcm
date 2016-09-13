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
NSString *const FCMLocalNotificationReceived = @"FCMLocalNotificationReceived";

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
  
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(handleFCMLocalNotificationReceived:)
                                               name:FCMLocalNotificationReceived
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

RCT_EXPORT_METHOD(getInitialNotification:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject){
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
  
  UIUserNotificationType types = UIUserNotificationTypeAlert | UIUserNotificationTypeBadge | UIUserNotificationTypeSound;
  
  UIApplication *app = RCTSharedApplication();
  if ([app respondsToSelector:@selector(registerUserNotificationSettings:)]) {
    UIUserNotificationSettings *notificationSettings =
    [UIUserNotificationSettings settingsForTypes:(NSUInteger)types categories:nil];
    [app registerUserNotificationSettings:notificationSettings];
    [app registerForRemoteNotifications];
  } else {
    [app registerForRemoteNotificationTypes:(NSUInteger)types];
  }
}

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

- (void)handleFCMLocalNotificationReceived:(UILocalNotification *)notification
{
  NSMutableDictionary *data = [[NSMutableDictionary alloc]initWithDictionary: notification.userInfo];
  [data setValue:@(RCTSharedApplication().applicationState == UIApplicationStateInactive) forKey:@"opened_from_tray"];
  [_bridge.eventDispatcher sendDeviceEventWithName:FCMLocalNotificationReceived body:data];
}

- (void)handleRemoteNotificationReceived:(NSNotification *)notification
{
  NSMutableDictionary *data = [[NSMutableDictionary alloc]initWithDictionary: notification.userInfo];
  [data setValue:@(RCTSharedApplication().applicationState == UIApplicationStateInactive) forKey:@"opened_from_tray"];
  [_bridge.eventDispatcher sendDeviceEventWithName:FCMNotificationReceived body:data];
}

@end
