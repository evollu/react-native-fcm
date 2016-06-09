import {NativeModules, DeviceEventEmitter} from 'react-native';

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

FCM.on = (event, callback) => {
    const nativeEvent = eventsMap[event];

    const listener = DeviceEventEmitter.addListener(nativeEvent, callback);

    return function remove() {
        listener.remove();
    };
};

FCM.subscribeToTopic = (topic) => {
    FIRMessaging.subscribeToTopic(topic);
}

FCM.unsubscribeFromTopic = (topic) => {
    FIRMessaging.unsubscribeFromTopic(topic);
}

FCM.initialData = FIRMessaging.initialData;
FCM.initialAction = FIRMessaging.initialAction;

module.exports = FCM;
