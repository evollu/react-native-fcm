import {
  NativeModules,
  DeviceEventEmitter,
} from 'react-native';

const eventsMap = {
  refersh: 'FCMTokenRefreshed',
  notification: 'FCMNotificationReceived',
};

const FIRMessaging = NativeModules.RNFIRMessaging;

const FCM = {};

FCM.getFCMToken = function getFCMToken() {
    return FIRMessaging.getFCMToken();
};

FCM.requestPermissions = function requestPermissions() {
  return FIRMessaging.requestPermissions();
};

FCM.on = function on(event, callback) {
  const nativeEvent = eventsMap[event];

  const listener = DeviceEventEmitter.addListener(nativeEvent, (params) => {
    callback(params);
  });

  return function remove() {
    listener.remove();
  };
};

FCM.initialData = FIRMessaging.initialData;
FCM.initialAction = FIRMessaging.initialAction;

module.exports = FCM;
