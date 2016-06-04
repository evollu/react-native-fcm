## Installation

- Run `npm install react-native-fcm --save`
- Run rnpm link

### Android Configuration

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

- In `android/app/src/main/AndroidManifest.xml`

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
#import "RNFIRMessaging.h"
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

## Usage

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

### Response to `click_action` in Android
To allow android to respond to `click_action`, you need to define Activities and filter on specific intent. Since everything is running in MainActivity, you can have MainActivity to handle actions.
```xml
<activity
  android:name=".MainActivity"
  android:label="@string/app_name"
  android:windowSoftInputMode="adjustResize"
  android:configChanges="keyboard|keyboardHidden|orientation|screenSize">
  <intent-filter>
      <action android:name="android.intent.action.MAIN" />
      <category android:name="android.intent.category.LAUNCHER" />
  </intent-filter>
    <intent-filter>                                                       <--add this line
        <action android:name="fcm.ACTION.HELLO" />                        <--add this line, name should match click_action
        <category android:name="android.intent.category.DEFAULT" />       <--add this line
    </intent-filter>                                                      <--add this line
</activity>
```
and pass intent into package, change MainActivity.java
```java
new FIRMessagingPackage(getIntent()),                                     <--add getIntent()
```

### Behaviour when sending `click_action` and `data` in notification
- When app is not running when user clicks notification, notification data will be passed into 
 - `FCM.initialAction`(contains `click_action` in notification payload
 - `FCM.initialData` (contains `data` payload if you send together with notification)

- When app is running in background
 - IOS will receive notificaton from `FCMNotificationReceived` event
 - Android will reload the whole react app

- When app is running in foreground
 - Both will receive notificaton from `FCMNotificationReceived` event

NOTE: it is recommend not to rely on `data` payload for click_action as it can be overwritten. check [this](http://stackoverflow.com/questions/33738848/handle-multiple-notifications-with-gcm)

## Q & A
#### My android build is failing
Try update your SDK and google play service
#### I can't get notification data when app is killed?
If you send notification with `data` only, you can only get the data message when app is in foreground or background. Killed app doesn't trigger FCMNotificationReceived. Seems that is how FCM works today
#### App running in background doesn't trigger `FCMNotificationReceived` when receiving hybrid notification [Android]
These is [an issue opened for that](https://github.com/google/gcm/issues/63). You will received data payload in FCM.initialData if you click on the notification. But if you just open the app, the data is lost.
#### I can't get `notification` payload when my android app is in foreground
It is better to use data payload if you need to pass data into application. I haven't implemented notification payload bridging in android module and am not planning to (the SDK should post notification for you)
#### Notification payload and data payload are mixed in iOS app.
I'm not doing any filtering. Try to add some `type` attributes to differentiate data payload from APN notification
#### App reloads when notification is clicked [Android]
Preserve app status with asyncStorage should get around this. Still looking for solution
### It is missing some features
Issues and pull requests are welcomed. Let's make this thing better!

