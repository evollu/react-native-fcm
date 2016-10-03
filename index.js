import {NativeModules, DeviceEventEmitter, Platform} from 'react-native';

const eventsMap = {
    refreshToken: 'FCMTokenRefreshed',
    notification: 'FCMNotificationReceived'
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
    details.local_notification = true;
    RNFIRMessaging.presentLocalNotification(details);
};

FCM.scheduleLocalNotification = function(details) {
    if (!details.id) {
        throw new Error("id is required for scheduled notification");
    }
    details.local_notification = true;
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
    if (!nativeEvent) {
        throw new Error('FCM invalid event');
    }
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

FCM.send = (senderId, payload) => {
    RNFIRMessaging.send(senderId, payload);
};

module.exports = FCM;
