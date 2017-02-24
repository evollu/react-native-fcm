[![Join the chat at https://gitter.im/evollu/react-native-fcm](https://badges.gitter.im/evollu/react-native-fcm.svg)](https://gitter.im/evollu/react-native-fcm?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## NOTE:
- for latest RN, use latest
- for RN < 0.40.0, use v2.5.6
- for RN < 0.33.0, you need to user react-native-fcm@1.1.0
- for RN < 0.30.0, you need to use react-native-fcm@1.0.15
- local notification is not only available in V1

- An example working project is available at: https://github.com/evollu/react-native-fcm/tree/master/Examples/simple-fcm-client

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

- Edit `android/app/build.gradle`. Add at the bottom of the file:
```diff
  apply plugin: "com.android.application"
+ apply plugin: 'com.google.gms.google-services'
```

- Edit `android/app/src/main/AndroidManifest.xml`:

```diff
  <application
    ...
    android:theme="@style/AppTheme">

+   <service android:name="com.evollu.react.fcm.MessagingService" android:enabled="true" android:exported="true">
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

- Edit `{YOUR_MAIN_PROJECT}/app/build.gradle`:
```diff
 dependencies {
     compile project(':react-native-fcm')
+    compile 'com.google.firebase:firebase-core:10.0.1' //this decides your firebase SDK version
     compile fileTree(dir: "libs", include: ["*.jar"])
     compile "com.android.support:appcompat-v7:23.0.1"
     compile "com.facebook.react:react-native:+"  // From node_modules
 }
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

NOTE: Verify that react-native links correctly in `MainApplication.java`

```diff
import android.app.application
...
+import com.evollu.react.fcm.FIRMessagingPackage;
```
....
```diff
    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new VectorIconsPackage(),
+         new FIRMessagingPackage(),
          new RNDeviceInfo(),
      );
    }
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
uncomment the "use_frameworks!" line in the podfile.

### Non Cocoapod approach

1. Download the Firebase SDK framework from [Integrate without CocoaPods](https://firebase.google.com/docs/ios/setup#frameworks).
- Import libraries, add Capabilities (background running and push notification), upload APNS and etc etc etc...
2. Follow the `README` to link frameworks (Analytics+Messaging)

### Shared steps
Make sure you have certificates setup by following
https://firebase.google.com/docs/cloud-messaging/ios/certs

Edit `AppDelegate.h`:
```diff
+ @import UserNotifications;
+
+ @interface AppDelegate : UIResponder <UIApplicationDelegate,UNUserNotificationCenterDelegate>
- @interface AppDelegate : UIResponder <UIApplicationDelegate>
```

Edit `AppDelegate.m`:
```diff
+ #import "RNFIRMessaging.h"
  //...

  - (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
  {
  //...
+   [FIRApp configure];
+   [[UNUserNotificationCenter currentNotificationCenter] setDelegate:self];
+ }
+
+ - (void)userNotificationCenter:(UNUserNotificationCenter *)center willPresentNotification:(UNNotification *)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions))completionHandler
+ {
+   [RNFIRMessaging willPresentNotification:notification withCompletionHandler:completionHandler];
+ }
+
+ - (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void (^)())completionHandler
+ {
+   [RNFIRMessaging didReceiveNotificationResponse:response withCompletionHandler:completionHandler];
+ }
+
+ //You can skip this method if you don't want to use local notification
+ -(void)application:(UIApplication *)application didReceiveLocalNotification:(UILocalNotification *)notification {
+   [RNFIRMessaging didReceiveLocalNotification:notification];
+ }
+
+ - (void)application:(UIApplication *)application didReceiveRemoteNotification:(nonnull NSDictionary *)userInfo fetchCompletionHandler:(nonnull void (^)(UIBackgroundFetchResult))completionHandler{
+   [RNFIRMessaging didReceiveRemoteNotification:userInfo fetchCompletionHandler:completionHandler];
+ }
```

### Xcode post installation steps
- Select your project **Capabilities** and enable **Keychan Sharing** and *Background Modes* > **Remote notifications**.

- In Xcode menu bar, select *Product* > *Scheme* > **Manage schemes**. Select your project name Scheme then click on the minus sign **―** in the bottom left corner, then click on the plus sign **+** and rebuild your project scheme.

### FCM config file

In [firebase console](https://console.firebase.google.com/), you can get `google-services.json` file and place it in `android/app` directory and get `GoogleService-Info.plist` file and place it in `/ios/your-project-name` directory (next to your `Info.plist`)

## Setup Local Notifications
NOTE: local notification does NOT have any dependency on FCM library but you still need to include Firebase to compile. If there are enough demand to use this functionality alone, I will separate it out into another repo

### IOS
No change required

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
import FCM, {FCMEvent, RemoteNotificationResult, WillPresentNotificationResult, NotificationType} from 'react-native-fcm';

class App extends Component {
    componentDidMount() {
        FCM.requestPermissions(); // for iOS
        FCM.getFCMToken().then(token => {
            console.log(token)
            // store fcm token in your server
        });
        this.notificationListener = FCM.on(FCMEvent.Notification, async (notif) => {
            // there are two parts of notif. notif.notification contains the notification payload, notif.data contains data payload
            if(notif.local_notification){
              //this is a local notification
            }
            if(notif.opened_from_tray){
              //app is open/resumed because user clicked banner
            }
            await someAsyncCall();
            
            if(Platform.OS ==='ios'){
              //optional
              //iOS requires developers to call completionHandler to end notification process. If you do not call it your background remote notifications could be throttled, to read more about it see the above documentation link. 
              //This library handles it for you automatically with default behavior (for remote notification, finish with NoData; for WillPresent, finish depend on "show_in_foreground"). However if you want to return different result, follow the following code to override
              //notif._notificationType is available for iOS platfrom
              switch(notif._notificationType){
                case NotificationType.Remote:
                  notif.finish(RemoteNotificationResult.NewData) //other types available: RemoteNotificationResult.NewData, RemoteNotificationResult.ResultFailed
                  break;
                case NotificationType.NotificationResponse:
                  notif.finish();
                  break;
                case NotificationType.WillPresent:
                  notif.finish(WillPresentNotificationResult.All) //other types available: WillPresentNotificationResult.None
                  break;
              }
            }
        });
        this.refreshTokenListener = FCM.on(FCMEvent.RefreshToken, (token) => {
            console.log(token)
            // fcm token may not be available on first load, catch it here
        });
    }

    componentWillUnmount() {
        // stop listening for events
        this.notificationListener.remove();
        this.refreshTokenListener.remove();
    }

    otherMethods(){

        FCM.subscribeToTopic('/topics/foo-bar');
        FCM.unsubscribeFromTopic('/topics/foo-bar');
        FCM.getInitialNotification().then(notif=>console.log(notif));
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
            icon: "ic_launcher",                                // as FCM payload, you can relace this with custom icon you put in mipmap
            big_text: "Show when notification is expanded",     // Android only
            sub_text: "This is a subText",                      // Android only
            color: "red",                                       // Android only
            vibrate: 300,                                       // Android only default: 300, no vibration if you pass null
            tag: 'some_tag',                                    // Android only
            group: "group",                                     // Android only
            my_custom_data:'my_custom_field_value',             // extra data you want to throw
            lights: true,                                       // Android only, LED blinking (default false)
            show_in_foreground                                  // notification when app is in foreground (local & remote)
        });

        FCM.scheduleLocalNotification({
            fire_date: new Date().getTime(),      //RN's converter is used, accept epoch time and whatever that converter supports
            id: "UNIQ_ID_STRING",    //REQUIRED! this is what you use to lookup and delete notification. In android notification with same ID will override each other
            body: "from future past",
            repeat_interval: "week" //day, hour
        })

        FCM.getScheduledLocalNotifications().then(notif=>console.log(notif));

        //these clears notification from notification center/tray
        FCM.removeAllDeliveredNotifications()
        FCM.removeDeliveredNotification("UNIQ_ID_STRING")

        //these removes future local notifications
        FCM.cancelAllLocalNotifications()
        FCM.cancelLocalNotification("UNIQ_ID_STRING")

        FCM.setBadgeNumber(1);                                       // iOS only and there's no way to set it in Android, yet.
        FCM.getBadgeNumber().then(number=>console.log(number));     // iOS only and there's no way to get it in Android, yet.
        FCM.send('984XXXXXXXXX', {
          my_custom_data_1: 'my_custom_field_value_1',
          my_custom_data_2: 'my_custom_field_value_2'
        });
    }
}
```

### Build custom push notification for Android
Firebase android misses important feature of android notification like `group`, `priority` and etc. As a work around you can send data message (no `notification` payload at all) and this repo will build a local notification for you. If you pass `custom_notification` in the payload, the repo will treat the content as a local notification config and shows immediately.

NOTE: By using this work around, you will have to send different types of payload for iOS and Android devices because custom_notification isn't supported on iOS

Example of payload that is sent to FCM server:
```
{
  "to":"FCM_TOKEN",
  "data": {
    "type":"MEASURE_CHANGE",
    "custom_notification": {
      "body": "test body",
      "title": "test title",
      "color":"#00ACD4",
      "priority":"high",
      "icon":"ic_notif",
      "group": "GROUP",
      "id": "id",
      "show_in_foreground": true
    }
  }
}
```

Check local notification guide below for configuration.

### Behaviour when sending `notification` and `data` payload through GCM
- When app is not running and user clicks notification, notification data will be passed into `FCM.getInitialNotification` event

- When app is running in background (the tricky one, I strongly suggest you try it out yourself)
 - IOS will receive notificaton from `FCMNotificationReceived` event
    * if you pass `content_available` flag true, you will receive one when app is in background and another one when user resume the app. [more info](http://www.rahuljiresal.com/2015/03/retract-push-notifications-on-ios/)
    * if you just pass `notification`, you will only receive one when user resume the app.
    * you will not see banner if `notification->body` is not defined.
 - Android will receive notificaton from `FCMNotificationReceived` event
    * if you pass `notification` payload, it will receive data when user click on notification
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
 - IOS will receive notification and android **won't** (better not to do anything in foreground for hybrid and send a separate data message.)

NOTE: it is recommended not to rely on `data` payload for click_action as it can be overwritten (check [this](http://stackoverflow.com/questions/33738848/handle-multiple-notifications-with-gcm)).

### Quick notes about upstream messages
If your app server implements the [XMPP Connection Server](https://firebase.google.com/docs/cloud-messaging/server#implementing-the-xmpp-connection-server-protocol) protocol, it can receive upstream messages from a user's device to the cloud. To initiate an upstream message, call the `FCM.send()` method with your Firebase `Sender ID` and a `Data Object` as parameters as follows:

```javascript
FCM.send('984XXXXXXXXX', {
  my_custom_data_1: 'my_custom_field_value_1',
  my_custom_data_2: 'my_custom_field_value_2'
});
```

The `Data Object` is message data comprising as many key-value pairs of the message's payload as are needed (ensure that the value of each pair in the data object is a `string`). Your `Sender ID` is a unique numerical value generated when you created your Firebase project, it is available in the `Cloud Messaging` tab of the Firebase console `Settings` pane. The sender ID is used to identify each app server that can send messages to the client app.

## Q & A

#### Why do you build another local notification
Yes there are `react-native-push-notification` and `react-native-system-notification` which are great libraries. However
- We want a unified local notification library but people are reporting using react-native-push-notification with this repo has compatibility issue as `react-native-push-notification` also sets up GCM.
- We want to have local notification to have similar syntax as remote notification payload.
- The PushNotificationIOS by react native team is still missing features that recurring, so we are adding it here

#### My Android build is failing
Try update your SDK and google play service. If you are having multiple plugins requiring different version of play-service sdk, use force to lock in version
```
dependencies {
    ...
    compile ('com.android.support:appcompat-v7:25.0.1') {
        exclude group: 'com.google.android', module: 'support-v4'
    }
    compile ('com.google.android.gms:play-services-gcm:10.0.1') {
        force = true;
    }
   ...
}
```

#### My App throws FCM function undefined error
There seems to be link issue with rnpm. Make sure that there is `new FIRMessagingPackage(),` in your `Application.java` file

#### I can't get notification in iOS emulator
Remote notification can't reach iOS emulator since it can't fetch APNS token. Use real device.

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

#### I'm getting `com.android.dex.DexException: Multiple dex files define Lcom/google/android/gms/internal/zzqf;`
It is most likely that you are using other react-native-modules that requires conflicting google play service
search for `compile "com.google.android.gms` in android and see who specifies specific version. Resolve conflict by loosing their version or specify a version resolve in gradle.

#### How do I tell if user clicks the notification banner?
Check open from tray flag in notification. It will be either 0 or 1 for iOS and undefined or 1 for android. I decide for iOS based on [this](http://stackoverflow.com/questions/20569201/remote-notification-method-called-twice), and for android I set it if notification is triggered by intent change.

#### Android notification doesn't vibrate/show head-up display etc
All available features are [here](https://firebase.google.com/docs/cloud-messaging/http-server-ref#notification-payload-support). FCM may add more support in the future but there is no timeline.
In the mean time, you can pass "custom_notification" in a data message. This repo will show a local notification for you so you can set priority etc

#### How do I do xxx with FCM?
check out [official docs and see if they support](https://firebase.google.com/docs/cloud-messaging/concept-options)

#### I want to add advanced feature that FCM doesn't support for remote notification
You can either wait for FCM to develop it or you have to write native code to create notifications.
- for iOS, you can do it in `didReceiveRemoteNotification` in `appDelegate.m`
- for android, you can do it by implementing a service similar to "com.evollu.react.fcm.MessagingService"

Or if you have a good way to wake up react native javascript thread please let me know, although I'm worring waking up the whole application is too expensive.

#### What about new notifications in iOS 10
Congratulations, now you have 5 notification handler to register!
in sum
- `willPresentNotification` is introduced in iOS 10 and will only be called when local/remote notification will show up. This allows you to run some code **before** notification shows up. You can also decide how to show the notification.
- `didReceiveNotificationResponse` is introduced in iOS 10 and provides user's response together with local/remote notification. It could be swipe, text input etc.
- `didReceiveLocalNotification` is for iOS 9 and below. Triggered when user clicks local notification. replaced by `didReceiveNotificationResponse`
- `didReceiveRemoteNotification` is for iOS 9 and below. Triggered when remote notification received.
- `didReceiveRemoteNotification:fetchCompletionHandler` is for both iOS 9 and 10. it gets triggered 2 times for each remote notification. 1st time when notification is received. 2nd time when notification is clicked. in iOS 9, it serves us the purpose of both `willPresentNotification` and `didReceiveNotificationResponse` but for remote notification only. in iOS 10, you don't need it in most of the case unless you need to do background fetching

Great, how do I configure for FCM?
It is up to you! FCM is just a bridging library that passes notification into javascript world. You can define your own NSDictionary and pass it into notification.

#### I want to show notification when app is in foreground
Use `show_in_foreground` attribute to tell app to show banner even if the app is in foreground.
NOTE: this flag doesn't work for Android push notification, use `custom_notification` to achieve this.

#### Do I need to handle APNS token registration?
No. Method swizzling in Firebase Cloud Messaging handles this unless you turn that off. Then you are on your own to implement the handling. Check this link https://firebase.google.com/docs/cloud-messaging/ios/client

#### I want to add actions in iOS notification
Check this https://github.com/evollu/react-native-fcm/issues/325

#### Some features are missing
Issues and pull requests are welcome. Let's make this thing better!

#### Credits
Local notification implementation is inspired by react-native-push-notification by zo0r
