# react-native-gcm-push-notification

Firebase notification for React Native Android and IOS

## Demo

https://github.com/oney/TestGcm

## Installation

- Run `npm install react-native-fcm --save`
- Run rnpm link

## Android Configuration

- In `android/build.gradle`
```gradle
dependencies {
classpath 'com.android.tools.build:gradle:2.0.0'
classpath 'com.google.gms:google-services:3.0.0' // <- Add this line
```

- In `android/app/build.gradle`
```gradle
apply plugin: "com.android.application"
apply plugin: 'com.google.gms.google-services' // <- Add this line
...
```

- In `android/app/src/main/AndroidManifest.xml`, add these lines, be sure to change `com.xxx.yyy` to your package

```
<application
android:theme="@style/AppTheme">

...
<service android:name="com.evollu.react.fcm.MessagingService">
<intent-filter>
<action android:name="com.google.firebase.MESSAGING_EVENT"/>
</intent-filter>
</service>

<service android:name="com.evollu.react.fcm.InstanceIdService" android:exported="false">
<intent-filter>
<action android:name="com.google.firebase.INSTANCE_ID_EVENT"/>
</intent-filter>
</service>
...
```

### IOS Configuration

install pod 'Firebase/Messaging'

in AppDelegate.m add
```
#import "FCMModule.h"
...

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
....
[FIRApp configure]; <-- add this line
}

//add this
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification {
[[NSNotificationCenter defaultCenter] postNotificationName: FCMNotificationReceived
object:self
userInfo:notification];

}

//add this
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))handler {
[[NSNotificationCenter defaultCenter] postNotificationName:FCMNotificationReceived
object:self
userInfo:notification];
handler(UIBackgroundFetchResultNoData);
}
```


### FCM config file
In [firebase console](https://console.firebase.google.com/), you can get `google-services.json` file and place it in `android/app` directory and get `googleServices-info.plist` file and place it in `/ios` directory

### Usage

```javascript

var FCM = require('react-native-fcm');

componentWillMount() {
FCM.requestPermissions();
FCM.getFCMToken().then(token => {
//store fcm token in your server
});
this.fcmNotifLsnr = DeviceEventEmitter.addListener('FCMNotificationReceived', (notif) => {
//there are two parts of notif. notif.notification contains the notification payload, notif.data contains data payload
});
this.fcmTokenLsnr = DeviceEventEmitter.addListener('FCMTokenRefreshed', (token) => {
//fcm token may not be available on first load, catch it here
});
}

componentWillUnmount() {
//prevent leak
this.fcmNotifLsnr.remove();
this.fcmTokenLsnr.remove();
}

}
```

## Q & A
### My android build is failing
Try update your SDK and google play service
### I can't get data notification when app is killed?
If you send notification with payload only, you can only get the data message when app is in foreground or background. Killed app can't show notification
### Why I don't get data notification when I'm sending hybrid notification when app is in background?
I want to figure it out too. Looks like that is how GCM/FCM works. I'm sending 2 notification separately for now. Let me know if you find a better solution

