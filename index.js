import {NativeModules, DeviceEventEmitter, Platform} from 'react-native';

const eventsMap = {
    refreshToken: 'FCMTokenRefreshed',
    notification: 'FCMNotificationReceived',
    localNotification: 'FCMLocalNotificationReceived'
};

const FIRMessaging = NativeModules.RNFIRMessaging;

const FCM = {};

FCM.getInitialNotification = () => {
    return FIRMessaging.getInitialNotification();
}

FCM.getFCMToken = () => {
    return FIRMessaging.getFCMToken();
};

FCM.requestPermissions = () => {
    return FIRMessaging.requestPermissions();
};

FCM.presentLocalNotification = (details) => {
    FIRMessaging.presentLocalNotification(details);
};

FCM.scheduleLocalNotification = function(details) {
    FIRMessaging.scheduleLocalNotification(details);
};

FCM.getScheduledLocalNotifications = function() {
    return FIRMessaging.getScheduledLocalNotifications();
};

FCM.cancelLocalNotification = (notificationID) => {
    FIRMessaging.cancelLocalNotification(notificationID);
};

FCM.cancelAllLocalNotifications = () => {
    FIRMessaging.cancelAllLocalNotifications();
};

FCM.setBadgeNumber = () => {
    FIRMessaging.setBadgeNumber();
}

FCM.getBadgeNumber = () => {
    return FIRMessaging.getBadgeNumber();
}

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
DeviceEventEmitter.addListener('FCMInitData', (data) => {
    FCM.initialData = data;
});

FCM.initialData = FIRMessaging.initialData;

module.exports = FCM;
