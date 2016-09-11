import {NativeModules, DeviceEventEmitter, Platform} from 'react-native';

const eventsMap = {
    refreshToken: 'FCMTokenRefreshed',
    notification: 'FCMNotificationReceived',
    localNotification: 'LocalNotificationReceived'
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
    FIRMessaging.presentLocalNotification({
  			alertBody: details.message,
  			alertAction: details.alertAction,
  			category: details.category,
  			soundName: soundName,
  			applicationIconBadgeNumber: details.number,
  			userInfo: details.userInfo
	   });
  }
};

FCM.scheduleLocalNotification = function(details) {
    var iosNotification = {
        fireDate: details.date.getTime(),
        alertBody: details.message,
        userInfo: details.userInfo || {},
        repeatInterval: REPEAT_INTERVAL_IOS[details.repeatEvery] || 0
    };
    if(details.id) {
      iosNotification.userInfo.id = details.id;
    }
    details.fireDate = details.date.getTime();
    delete details.date;

    FIRMessaging.scheduleLocalNotification((Platform.OS === 'ios')? iosNotification : details);
};

FCM.cancelLocalNotification = (notificationID) => {
        FIRMessaging.cancelLocalNotification(notificationID);
};

FCM.cancelLocalNotifications = () => {
    FIRMessaging.cancelLocalNotifications();
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
