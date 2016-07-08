[![Join the chat at https://gitter.im/evollu/react-native-fcm](https://badges.gitter.im/evollu/react-native-fcm.svg)](https://gitter.im/evollu/react-native-fcm?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Installation

- Run `npm install react-native-fcm --save`
- Run `rnpm link`

Or you can combine 2 commands
- Run `rnpm install react-native-fcm`

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

// Add this line to update intent on notification click
@Override
// in RN <= 0.27 you may need to use `protected void onNewIntent (Intent intent) {`
public void onNewIntent (Intent intent) {
  super.onNewIntent(intent);
    setIntent(intent);
}       

// Add package
@Override
    protected List<ReactPackage> getPackages() {
    ...
      new FIRMessagingPackage(getIntent()),                               // <-- add getIntent()
    ...
```

## IOS Configuration

### Pod approach:

install pod 'Firebase/Messaging'
NOTE: make sure cocoapods version > 1.0
```
cd ios && pod init
pod install Firebase/Messaging
```

### Non Cocoapod approach
1. download framework from https://firebase.google.com/docs/ios/setup last section Integrate without CocoaPods
2. Follow the readme to link frameworks (Analytics+Messaging)
3. current zip file is missing `FirebaseAnalytics.framework` file. I put one in the root of this repository

### Shared steps
in AppDelegate.m add
```
#import "Firebase.h" <--add if you are using Non Cocoapod approach
#import "RNFIRMessaging.h" <--add this line
...

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
....
  [FIRApp configure]; <-- add this line
}

//add this method
- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))handler {
  [[NSNotificationCenter defaultCenter] postNotificationName:FCMNotificationReceived object:self userInfo:notification];
  handler(UIBackgroundFetchResultNoData);
}
```


### FCM config file
In [firebase console](https://console.firebase.google.com/), you can get `google-services.json` file and place it in `android/app` directory and get `googleServices-info.plist` file and place it in `/ios` directory

## Usage

```javascript
  import FCM from 'react-native-fcm';

  ...
  componentWillMount() {
    FCM.requestPermissions();
    FCM.getFCMToken().then(token => {
      console.log(token)
      // store fcm token in your server
    });
    this.notificationUnsubscribe = FCM.on('notification', (notif) => {
      // there are two parts of notif. notif.notification contains the notification payload, notif.data contains data payload
    });
    this.refreshUnsubscribe = FCM.on('refreshToken', (token) => {
      console.log(token)
      // fcm token may not be available on first load, catch it here
    });
    
    FCM.subscribeToTopic('/topics/foo-bar');
    FCM.unsubscribeFromTopic('/topics/foo-bar');
  }

  componentWillUnmount() {
    // prevent leak
    this.refreshUnsubscribe();
    this.notificationUnsubscribe();
  }
  ...
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
If you send notification with `data` only, you can only get the data message when app is in foreground or background. Killed app doesn't trigger FCMNotificationReceived. Use `notification` in the payload instead
#### App running in background doesn't trigger `FCMNotificationReceived` when receiving hybrid notification [Android]
These is [an issue opened for that](https://github.com/google/gcm/issues/63). Behavior is not consistent between 2 platforms
#### Android notification is showing a white icon
Since Lolipop, push notification icon is required to be all white, otherwise it will be a white circle.
#### It is missing some features
Issues and pull requests are welcomed. Let's make this thing better!

