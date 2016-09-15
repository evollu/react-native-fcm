import {NativeModules, DeviceEventEmitter, Platform} from 'react-native';

const eventsMap = {
    refreshToken: 'FCMTokenRefreshed',
    notification: 'FCMNotificationReceived',
    localNotification: 'FCMLocalNotificationReceived'
};

const RNFIRMessaging = NativeModules.RNFIRMessaging;

const FCM = {};

FCM.getInitialNotification = () => {
    return RNFIRMessaging.getInitialNotification();
}

FCM.getFCMToken = () => {
    return RNFIRMessaging.getFCMToken();
};

FCM.requestPermissions = () => {
    return RNFIRMessaging.requestPermissions();
};

FCM.presentLocalNotification = (details) => {
    RNFIRMessaging.presentLocalNotification(details);
};

FCM.scheduleLocalNotification = function(details) {
    RNFIRMessaging.scheduleLocalNotification(details);
};

FCM.getScheduledLocalNotifications = function() {
    return RNFIRMessaging.getScheduledLocalNotifications();
};

FCM.cancelLocalNotification = (notificationID) => {
    RNFIRMessaging.cancelLocalNotification(notificationID);
};

FCM.cancelAllLocalNotifications = () => {
    RNFIRMessaging.cancelAllLocalNotifications();
};

FCM.setBadgeNumber = (number) => {
    RNFIRMessaging.setBadgeNumber(number);
}

FCM.getBadgeNumber = () => {
    return RNFIRMessaging.getBadgeNumber();
}

FCM.on = (event, callback) => {
    const nativeEvent = eventsMap[event];
    const listener = DeviceEventEmitter.addListener(nativeEvent, callback);

    return function remove() {
        listener.remove();
    };
};

FCM.subscribeToTopic = (topic) => {
    RNFIRMessaging.subscribeToTopic(topic);
};

FCM.unsubscribeFromTopic = (topic) => {
    RNFIRMessaging.unsubscribeFromTopic(topic);
};

module.exports = FCM;
