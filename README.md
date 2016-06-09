## Installation

- Run `npm install react-native-fcm --save`
- Run `rnpm link`

Or you can combine 2 commands
- Run `rnpm install react-native-fcm --save`

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
### Config for notification and `click_action` in Android
To allow android to respond to `click_action`, you need to define Activities and filter on specific intent. Since all javascript is running in MainActivity, you can have MainActivity to handle actions.
```xml
<activity
  android:name=".MainActivity"
  android:label="@string/app_name"
  android:windowSoftInputMode="adjustResize"
  android:launchMode="singleTop"                                          <--add this line to reuse MainActivity
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
import android.content.Intent;                                            <--add this line next to the other imports

@Override                                                                 <--add this line
protected void onNewIntent(Intent intent){                                <--add this line
    setIntent(intent);                                             <--add this line to update intent on notification click
}                                                                         <--add this line

@Override
    protected List<ReactPackage> getPackages() {
    ...
      new FIRMessagingPackage(getIntent()),                               <--add getIntent()
    ...
```

## IOS Configuration

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

//add this method
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification {
[[NSNotificationCenter defaultCenter] postNotificationName: FCMNotificationReceived
object:self
userInfo:notification];

}
```


### FCM config file
In [firebase console](https://console.firebase.google.com/), you can get `google-services.json` file and place it in `android/app` directory and get `googleServices-info.plist` file and place it in `/ios` directory

## Usage

```javascript

import {DeviceEventEmitter} from 'react-native';
var FCM = require('react-native-fcm');

componentWillMount() {
FCM.requestPermissions();
FCM.getFCMToken().then(data => {
  console.log(data.token)
//store fcm token in your server
});
this.fcmNotifLsnr = DeviceEventEmitter.addListener('FCMNotificationReceived', (notif) => {
//there are two parts of notif. notif.notification contains the notification payload, notif.data contains data payload
});
this.fcmTokenLsnr = DeviceEventEmitter.addListener('FCMTokenRefreshed', (data) => {
  console.log(data.token)
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

### Behaviour when sending `notification` and `data` payload through GCM
- When app is not running when user clicks notification, notification data will be passed into 
 - `FCM.initialAction`(contains `click_action` in notification payload
 - `FCM.initialData` (contains `data` payload if you send together with notification)

- When app is running in background
 - App will receive notificaton from `FCMNotificationReceived` event when user click on notification.
   e.g. fcm payload looks like
   ```
   {
      "to":"some_device_token",
      "content_available": true,
      "notification": {
          "title": "hello",
          "body": "yo",
          "click_action": "fcm.ACTION.HELLO"
      },
      "data": {
          "extra":"juice"
      }
    }
    ```
    and event callback will receive as
    ```
    ///Android
    {
      fcm: {"action": "fcm.ACTION.HELLO"},
      extra: "juice"
    }
    ///IOS
    {
      apns: {action_category: "fcm.ACTION.HELLO"},
      extra: "juice"
    }
    ```

- When app is running in foreground
 - IOS will receive notification and android **won't** (better not to do anything in foreground for hybrid and send a seprate data message.)

NOTE: it is recommend not to rely on `data` payload for click_action as it can be overwritten. check [this](http://stackoverflow.com/questions/33738848/handle-multiple-notifications-with-gcm)

## Q & A
#### My android build is failing
Try update your SDK and google play service
#### I can't get notification when app is killed
If you send notification with `data` only, you can only get the data message when app is in foreground or background. Killed app doesn't trigger FCMNotificationReceived. Seems that is how FCM works today
#### App running in background doesn't trigger `FCMNotificationReceived` when receiving hybrid notification [Android]
These is [an issue opened for that](https://github.com/google/gcm/issues/63). Behavior is not consistent between 2 platforms
#### It is missing some features
Issues and pull requests are welcomed. Let's make this thing better!

