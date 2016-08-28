import {NativeModules, DeviceEventEmitter, AppState, Platform, PushNotificationIOS} from 'react-native';

const eventsMap = {
    refreshToken: 'FCMTokenRefreshed',
    notification: 'FCMNotificationReceived'
};

const FIRMessaging = NativeModules.RNFIRMessaging;

const FCM = {};

FCM.getFCMToken = () => {
    return FIRMessaging.getFCMToken();
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
    const nativeEvent = eventsMap[event];

    const listener = DeviceEventEmitter.addListener(nativeEvent, callback);

    return function remove() {
        listener.remove();
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
