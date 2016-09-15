[![Join the chat at https://gitter.im/evollu/react-native-fcm](https://badges.gitter.im/evollu/react-native-fcm.svg)](https://gitter.im/evollu/react-native-fcm?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## NOTE: 
- If you are running RN < 0.30.0, you need to use react-native-fcm@1.0.15
- If you are running RN < 0.33.0, you need to user react-native-fcm@1.1.0

## Installation

- Run `npm install react-native-fcm --save`
- Run `react-native link react-native-fcm` (RN 0.29.1+, otherwise `rnpm link react-native-fcm`)

## Android Configuration

- Edit `android/build.gradle`:
```diff
  dependencies {
    classpath 'com.android.tools.build:gradle:2.0.0'
+   classpath 'com.google.gms:google-services:3.0.0'
```

- Edit `android/app/build.gradle`:
```diff
  apply plugin: "com.android.application"
+ apply plugin: 'com.google.gms.google-services'
```

- Edit `android/app/src/main/AndroidManifest.xml`:

```diff
  <application
    ...
    android:theme="@style/AppTheme">

+   <service android:name="com.evollu.react.fcm.MessagingService">
+     <intent-filter>
+       <action android:name="com.google.firebase.MESSAGING_EVENT"/>
+     </intent-filter>
+   </service>

+   <service android:name="com.evollu.react.fcm.InstanceIdService" android:exported="false">
+     <intent-filter>
+       <action android:name="com.google.firebase.INSTANCE_ID_EVENT"/>
+     </intent-filter>
+   </service>

    ...
```

### Config for notification and `click_action` in Android

To allow android to respond to `click_action`, you need to define Activities and filter on specific intent. Since all javascript is running in MainActivity, you can have MainActivity to handle actions:

Edit `AndroidManifest.xml`:

```diff
  <activity
    android:name=".MainActivity"
    android:label="@string/app_name"
    android:windowSoftInputMode="adjustResize"
+   android:launchMode="singleTop"
    android:configChanges="keyboard|keyboardHidden|orientation|screenSize">
    <intent-filter>
      <action android:name="android.intent.action.MAIN" />
      <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
+   <intent-filter>
+     <action android:name="fcm.ACTION.HELLO" />
+     <category android:name="android.intent.category.DEFAULT" />
+   </intent-filter>
  </activity>
```

Notes:
- `launchMode="singleTop"` is to reuse MainActivity
- replace `"fcm.ACTION.HELLO"` by the `click_action` you want to match


If you are using RN < 0.30.0 and react-native-fcm < 1.0.16, pass intent into package, edit `MainActivity.java`:

- RN 0.28:

```diff
  import com.facebook.react.ReactActivity;
+ import android.content.Intent;

  public class MainActivity extends ReactActivity {

+   @Override
+   public void onNewIntent (Intent intent) {
+     super.onNewIntent(intent);
+       setIntent(intent);
+   }       
```

- RN <= 0.27:

```diff
  import com.facebook.react.ReactActivity;
+ import android.content.Intent;

  public class MainActivity extends ReactActivity {

+   @Override
+   protected void onNewIntent (Intent intent) {
+     super.onNewIntent(intent);
+       setIntent(intent);
+   }       
```

Notes:
- `@Override` is added to update intent on notification click

## IOS Configuration

### Pod approach:

Make sure you have Cocoapods version > 1.0

Install the `Firebase/Messaging` pod:
```
cd ios && pod init
pod install Firebase/Messaging
```

### Non Cocoapod approach

1. Download the Firebase SDK framework from [Integrate without CocoaPods](https://firebase.google.com/docs/ios/setup#frameworks)
2. Follow the `README` to link frameworks (Analytics+Messaging)

### Shared steps

Edit `AppDelegate.m`:
```diff
+ #import "Firebase.h" // if you are using Non Cocoapod approach
+ #import "RNFIRMessaging.h"
  //...

  - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
  {
  //...
+   [FIRApp configure];
  }

+ - (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)notification fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))handler {
+   [[NSNotificationCenter defaultCenter] postNotificationName:FCMNotificationReceived object:self userInfo:notification];
+   handler(UIBackgroundFetchResultNewData);
+ }
```

### FCM config file

In [firebase console](https://console.firebase.google.com/), you can get `google-services.json` file and place it in `android/app` directory and get `GoogleService-Info.plist` file and place it in `/ios/your-project-name` directory (next to your `Info.plist`)
 
## Setup Local Notifications
NOTE: local notification does NOT have any dependency on FCM library but you still need to include Firebase to compile. If there are enough demand to use this functionality alone, I will separate it out into another repo

### IOS

Edit Appdelegate.m
```diff
+ -(void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification 
+ {
+   [[NSNotificationCenter defaultCenter] postNotificationName:FCMLocalNotificationReceived object:self userInfo:notification.userInfo];
+ }
```
 
### Android
Edit AndroidManifest.xml
```diff
  <uses-permission android:name="android.permission.INTERNET" />
+ <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
+ <uses-permission android:name="android.permission.VIBRATE" />
 
  <application
+      <receiver android:name="com.evollu.react.fcm.FIRLocalMessagingPublisher"/>
+      <receiver android:enabled="true" android:exported="true"  android:name="com.evollu.react.fcm.FIRSystemBootEventReceiver">
+          <intent-filter>
+              <action android:name="android.intent.action.BOOT_COMPLETED"/>
+              <action android:name="android.intent.action.QUICKBOOT_POWERON"/>
+              <action android:name="com.htc.intent.action.QUICKBOOT_POWERON"/>
+              <category android:name="android.intent.category.DEFAULT" />
+          </intent-filter>
+      </receiver>
  </application>
``` 
NOTE: `com.evollu.react.fcm.FIRLocalMessagingPublisher` is required for presenting local notifications. `com.evollu.react.fcm.FIRSystemBootEventReceiver` is required only if you need to schedule future or recurring local notifications


## Usage

```javascript
import FCM from 'react-native-fcm';

class App extends Component {
    componentDidMount() {
        FCM.requestPermissions(); // for iOS
        FCM.getFCMToken().then(token => {
            console.log(token)
            // store fcm token in your server
        });
        this.notificationUnsubscribe = FCM.on('notification', (notif) => {
            // there are two parts of notif. notif.notification contains the notification payload, notif.data contains data payload
        });
        this.localNotificationUnsubscribe = FCM.on('localNotification', (notif) => {
            // notif.notification contains the data
        });
        this.refreshUnsubscribe = FCM.on('refreshToken', (token) => {
            console.log(token)
            // fcm token may not be available on first load, catch it here
        });
    }

    componentWillUnmount() {
        // prevent leaking
        this.refreshUnsubscribe();
        this.notificationUnsubscribe();
        this.localNotificationUnsubscribe();
    }
 
    otherMethods(){
        FCM.subscribeToTopic('/topics/foo-bar');
        FCM.unsubscribeFromTopic('/topics/foo-bar');
        FCM.getInitialNotification().then(...);
        FCM.presentLocalNotification({
            id: "UNIQ_ID_STRING",                               // (optional for instant notification)
            title: "My Notification Title",                     // as FCM payload
            body: "My Notification Message",                    // as FCM payload (required)
            sound: "default",                                   // as FCM payload
            priority: "high",                                   // as FCM payload
            click_action: "ACTION",                             // as FCM payload
            badge: 10,                                          // as FCM payload IOS only, set 0 to clear badges
            number: 10,                                         // Android only
            ticker: "My Notification Ticker",                   // Android only
            auto_cancel: true,                                  // Android only (default true)
            large_icon: "ic_launcher",                           // Android only
            icon: "ic_notification",                            // as FCM payload
            big_text: "Show when notification is expanded",     // Android only
            sub_text: "This is a subText",                      // Android only
            color: "red",                                       // Android only
            vibrate: 300,                                       // Android only default: 300, no vibration if you pass null
            tag: 'some_tag',                                    // Android only
            group: "group",                                     // Android only
            my_custom_data:'my_custom_field_value',             // extra data you want to throw
        });
 
        FCM.scheduleLocalNotification({
            fire_date: new Date().getTime(),      //react convert is used, accept epoch time or ISO string
            id: "UNIQ_ID_STRING",    //REQUIRED! this is what you use to lookup and delete notification. In android notification with same ID will override each other
            body: "from future past",
            repeat_interval: "week" //day, hour
        })
 
        FCM.getScheduledLocalNotifications().then(...);
        FCM.cancelLocalNotification("UNIQ_ID_STRING");
        FCM.cancelAllLocalNotifications();
        FCM.setBadgeNumber();
        FCM.getBadgeNumber().then(...);
    }
}
```

### Behaviour when sending `notification` and `data` payload through GCM
- When app is not running and user clicks notification, notification data will be passed into `FCM.initialData`

- When app is running in background (the tricky one, I strongly suggest you try it out yourself)
 - IOS will receive notificaton from `FCMNotificationReceived` event
    * if you pass `content_available` flag true, you will receive one when app is in background and another one when user resume the app. [more info](http://www.rahuljiresal.com/2015/03/retract-push-notifications-on-ios/)
    * if you just pass `notification`, you will only receive one when user resume the app.
    * you will not see banner if `notification->body` is not defined.
 - Android will receive notificaton from `FCMNotificationReceived` event
    * if you pass `notification` payload. it will receive data when user click on notification
    * if you pass `data` payload only, it will receive data when in background

   e.g. fcm payload looks like:

   ```json
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

    and event callback will receive as:
    
    - Android
      ```json
      {
        "fcm": {"action": "fcm.ACTION.HELLO"},
        "opened_from_tray": 1,
        "extra": "juice"
      }
      ```
    
    - iOS
      ```json
      {
        "apns": {"action_category": "fcm.ACTION.HELLO"},
        "opened_from_tray": 1,
        "extra": "juice"
      }
      ```

- When app is running in foreground
 - IOS will receive notification and android **won't** (better not to do anything in foreground for hybrid and send a seprate data message.)

NOTE: it is recommend not to rely on `data` payload for click_action as it can be overwritten (check [this](http://stackoverflow.com/questions/33738848/handle-multiple-notifications-with-gcm)).

## Q & A

#### Why do you build another local notification
Yes there are `react-native-push-notification` and `react-native-system-notification` which are great libraries. However
- We want a unified local notification library but people are reporting using react-native-push-notification with this repo has compatibility issue as `react-native-push-notification` also sets up GCM.
- We want to have local notification to have similar syntax as remote notification payload.
- The PushNotificationIOS by react native team is still missing features that recurring, so we are adding it here

#### My Android build is failing
Try update your SDK and google play service

#### I can't get notification when app is killed
If you send notification with `data` only, you can only get the data message when app is in foreground or background. Killed app doesn't trigger `FCMNotificationReceived`. Use `notification` in the payload instead.

#### App running in background doesn't trigger `FCMNotificationReceived` when receiving hybrid notification [Android]
These is [an issue opened for that](https://github.com/google/gcm/issues/63). Behavior is not consistent between 2 platforms

#### Android notification is showing a white icon
Since Lollipop, the push notification icon is required to be all white, otherwise it will be a white circle.

#### iOS not receiving notification when the app running in the background
- Try adding Background Modes permission in Xcode->Click on project file->Capabilities tab->Background Modes->Remote Notifications

#### I am using Proguard
You need to add this to your `android/app/proguard-rules.pro`:
```
# Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
```

#### How do I tell if user clicks the notification banner?
Check open from tray flag in notification. It will be either 0 or 1 for iOS and undefined or 1 for android. I decide for iOS base on [this](http://stackoverflow.com/questions/20569201/remote-notification-method-called-twice), and for android I set it if notification is triggered by intent change.

#### Android notification doesn't vibrate/show head-up display etc
All available features are [here](https://firebase.google.com/docs/cloud-messaging/http-server-ref#notification-payload-support). FCM may add more support in the future but there is no timeline. If you need these features now, send notification with `data` only and creating notification locally is the only way.
Or you can send `data` using FCM and build a local notification

#### Some features are missing
Issues and pull requests are welcome. Let's make this thing better!
 
#### Thanks
Local notification implementation is inspired by react-native-push-notification by zo0r
