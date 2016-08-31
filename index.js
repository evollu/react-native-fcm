import {NativeModules, DeviceEventEmitter, AppState, Platform, PushNotificationIOS} from 'react-native';

const eventsMap = {
    refreshToken: 'FCMTokenRefreshed',
    notification: 'FCMNotificationReceived'
};

let currentAppState = 'active';

AppState.addEventListener('change', (appState)=>{
  currentAppState = appState;
});

const FIRMessaging = NativeModules.RNFIRMessaging;

const FCM = {};

FCM.getFCMToken = () => {
    return FIRMessaging.getFCMToken();
};

FCM.getInitFCMData = () => {
    return FIRMessaging.getInitFCMData().then((notification)=>{
      if (notification === null){
        return null;
      }
      else {
        notification['opened_from_tray'] = true;
        return notification;
      }
    });
};

FCM.requestPermissions = () => {
    return FIRMessaging.requestPermissions();
};

FCM.presentLocalNotification = (details) =>{
  if (Platform.OS ==='android'){
      FIRMessaging.presentLocalNotification(details);
  }
  else if (Platform.OS ==='ios') {
    const soundName = !details.hasOwnProperty("playSound") || details.playSound === true ? 'default' : '';// empty string results in no sound
    PushNotificationIOS.presentLocalNotification({
			alertBody: details.message,
			alertAction: details.alertAction,
			category: details.category,
			soundName: soundName,
			applicationIconBadgeNumber: details.number,
			userInfo: details.userInfo
	   });
  }
};

FCM.cancelAll = () => {
  if (Platform.OS ==='android'){
    FIRMessaging.cancelAll();
  }
  else if (Platform.OS ==='ios') {
    PushNotificationIOS.cancelLocalNotifications();
    PushNotificationIOS.setApplicationIconBadgeNumber(0);
  }
};


FCM.on = (event, callback) => {
    const nativeEvent = eventsMap[event]

    const onNotificationReceived = (notif)=>{
      if (notif === null){
        callback(notif);
        return;
      }
      notif['opened_from_tray'] = currentAppState !== 'active';
      callback(notif);
    }

    if (Platform.OS === 'ios') {
      PushNotificationIOS.addEventListener('localNotification', onNotificationReceived);
    }

    const listener = DeviceEventEmitter.addListener(nativeEvent, onNotificationReceived);
    return function remove() {
        listener.remove();
        if (Platform.OS ==='ios') {
          PushNotificationIOS.removeEventListener('localNotification', onNotificationReceived);
        }
    };
};

FCM.subscribeToTopic = (topic) => {
    FIRMessaging.subscribeToTopic(topic);
};

FCM.unsubscribeFromTopic = (topic) => {
    FIRMessaging.unsubscribeFromTopic(topic);
};

//once doesn't seem to work
DeviceEventEmitter.addListener('FCMInitData', (data)=>{
  FCM.initialData = data;
});

FCM.initialData = FIRMessaging.initialData;

module.exports = FCM;
