import {NativeModules, DeviceEventEmitter} from 'react-native';

const eventsMap = {
    refreshToken: 'FCMTokenRefreshed',
    notification: 'FCMNotificationReceived'
};

const FIRMessaging = NativeModules.RNFIRMessaging;

const FCM = {};

FCM.getFCMToken = () => {
    if (!FIRMessaging) return;
    return FIRMessaging.getFCMToken();
};

FCM.requestPermissions = () => {
    if (!FIRMessaging) return;
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
    if (!FIRMessaging) return;
    FIRMessaging.subscribeToTopic(topic);
};

FCM.unsubscribeFromTopic = (topic) => {
    if (!FIRMessaging) return;
    FIRMessaging.unsubscribeFromTopic(topic);
};

//once doesn't seem to work
DeviceEventEmitter.addListener('FCMInitData', (data)=>{
  FCM.initialData = data;
});

if (FIRMessaging){
    FCM.initialData = FIRMessaging.initialData;
}


module.exports = FCM;
