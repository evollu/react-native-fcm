import {NativeModules, DeviceEventEmitter, AppState, Platform, PushNotificationIOS} from 'react-native';

const eventsMap = {
    refreshToken: 'FCMTokenRefreshed',
    notification: 'FCMNotificationReceived'
};

const REPEAT_INTERVAL_IOS = {
     year: 4,
     month: 8,
     week: 8192,
     day: 16,
     hour: 32,
     minute: 64
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

FCM.localNotificationSchedule = function(details) {
	if ( Platform.OS === 'ios' ) {
    var notification = {
			fireDate: details.date.getTime(),
			alertBody: details.message,
			userInfo: details.userInfo,
      repeatInterval: REPEAT_INTERVAL_IOS[details.repeatEvery] || 0
		};
		FIRMessaging.scheduleLocalNotification(notification);
	} else {
		details.fireDate = details.date.getTime();
		delete details.date;
		FIRMessaging.scheduleLocalNotification(details);
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

    if(Platform.OS === 'ios'){
        PushNotificationIOS.addEventListener('localNotification', callback);
    }
    const listener = DeviceEventEmitter.addListener(nativeEvent, callback);

    return function remove() {
        listener.remove();
        if(Platform.OS === 'ios'){
            PushNotificationIOS.removeEventListener('localNotification', callback);
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
