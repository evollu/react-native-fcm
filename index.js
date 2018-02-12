import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

const EventEmitter = new NativeEventEmitter(NativeModules.RNFIRMessaging || {});

export const FCMEvent = {
  RefreshToken: 'FCMTokenRefreshed',
  Notification: 'FCMNotificationReceived',
  DirectChannelConnectionChanged: 'FCMDirectChannelConnectionChanged'
};

export const RemoteNotificationResult = {
  NewData: 'UIBackgroundFetchResultNewData',
  NoData: 'UIBackgroundFetchResultNoData',
  ResultFailed: 'UIBackgroundFetchResultFailed'
};

export const WillPresentNotificationResult = {
  All: 'UNNotificationPresentationOptionAll',
  None: 'UNNotificationPresentationOptionNone'
};

export const NotificationType = {
  Remote: 'remote_notification',
  NotificationResponse: 'notification_response',
  WillPresent: 'will_present_notification',
  Local: 'local_notification'
};

export const NotificationCategoryOption = {
  CustomDismissAction: 'UNNotificationCategoryOptionCustomDismissAction',
  AllowInCarPlay: 'UNNotificationCategoryOptionAllowInCarPlay',
  PreviewsShowTitle: 'UNNotificationCategoryOptionHiddenPreviewsShowTitle',
  PreviewsShowSubtitle: 'UNNotificationCategoryOptionHiddenPreviewsShowSubtitle',
  None: 'UNNotificationCategoryOptionNone'
};

export const NotificationActionOption = {
  AuthenticationRequired: 'UNNotificationActionOptionAuthenticationRequired',
  Destructive: 'UNNotificationActionOptionDestructive',
  Foreground: 'UNNotificationActionOptionForeground',
  None: 'UNNotificationActionOptionNone',
};

export const NotificationActionType = {
  Default: 'UNNotificationActionTypeDefault',
  TextInput: 'UNNotificationActionTypeTextInput',
};

const RNFIRMessaging = NativeModules.RNFIRMessaging;

const FCM = {};

FCM.getInitialNotification = () => {
  return RNFIRMessaging.getInitialNotification();
};

FCM.enableDirectChannel = () => {
  if (Platform.OS === 'ios') {
    return RNFIRMessaging.enableDirectChannel();
  }
};

FCM.isDirectChannelEstablished = () => {
  return Platform.OS === 'ios' ? RNFIRMessaging.isDirectChannelEstablished() : Promise.resolve(true);
};

FCM.getFCMToken = () => {
  return RNFIRMessaging.getFCMToken();
};

FCM.getEntityFCMToken = () => {
  return RNFIRMessaging.getEntityFCMToken();
}

FCM.deleteEntityFCMToken = () => {
  return RNFIRMessaging.deleteEntityFCMToken();
}

FCM.deleteInstanceId = () =>{
  return RNFIRMessaging.deleteInstanceId();
};

FCM.getAPNSToken = () => {
  if (Platform.OS === 'ios') {
    return RNFIRMessaging.getAPNSToken();
  }
};

FCM.requestPermissions = () => {
  return RNFIRMessaging.requestPermissions();
};

FCM.presentLocalNotification = (details) => {
  details.id = details.id || new Date().getTime().toString();
  details.local_notification = true;
  RNFIRMessaging.presentLocalNotification(details);
};

FCM.scheduleLocalNotification = function(details) {
  if (!details.id) {
    throw new Error('id is required for scheduled notification');
  }
  details.local_notification = true;
  RNFIRMessaging.scheduleLocalNotification(details);
};

FCM.getScheduledLocalNotifications = function() {
  return RNFIRMessaging.getScheduledLocalNotifications();
};

FCM.cancelLocalNotification = (notificationID) => {
  if (!notificationID) {
    return;
  }
  RNFIRMessaging.cancelLocalNotification(notificationID);
};

FCM.cancelAllLocalNotifications = () => {
  RNFIRMessaging.cancelAllLocalNotifications();
};

FCM.removeDeliveredNotification = (notificationID) => {
  if (!notificationID) {
    return;
  }
  RNFIRMessaging.removeDeliveredNotification(notificationID);
};

FCM.removeAllDeliveredNotifications = () => {
  RNFIRMessaging.removeAllDeliveredNotifications();
};

FCM.setBadgeNumber = (number) => {
  RNFIRMessaging.setBadgeNumber(number);
};

FCM.getBadgeNumber = () => {
  return RNFIRMessaging.getBadgeNumber();
};

function finish(result) {
  if (Platform.OS !== 'ios') {
    return;
  }
  if (!this._finishCalled && this._completionHandlerId) {
    this._finishCalled = true;
    switch (this._notificationType) {
      case NotificationType.Remote:
        result = result || RemoteNotificationResult.NoData;
        if (!Object.values(RemoteNotificationResult).includes(result)) {
          throw new Error(`Invalid RemoteNotificationResult, use import {RemoteNotificationResult} from 'react-native-fcm' to avoid typo`);
        }
        RNFIRMessaging.finishRemoteNotification(this._completionHandlerId, result);
        return;
      case NotificationType.NotificationResponse:
        RNFIRMessaging.finishNotificationResponse(this._completionHandlerId);
        return;
      case NotificationType.WillPresent:
        result = result || (this.show_in_foreground ? WillPresentNotificationResult.All : WillPresentNotificationResult.None);
        if (!Object.values(WillPresentNotificationResult).includes(result)) {
          throw new Error(`Invalid WillPresentNotificationResult, make sure you use import {WillPresentNotificationResult} from 'react-native-fcm' to avoid typo`);
        }
        RNFIRMessaging.finishWillPresentNotification(this._completionHandlerId, result);
        return;
      default:
        return;
    }
  }
}

FCM.on = (event, callback) => {
  if (!Object.values(FCMEvent).includes(event)) {
    throw new Error(`Invalid FCM event subscription, use import {FCMEvent} from 'react-native-fcm' to avoid typo`);
  };

  if (event === FCMEvent.Notification) {
    return EventEmitter.addListener(event, async(data) => {
      data.finish = finish;
      try {
        await callback(data);
      } catch (err) {
        console.error('Notification handler err', err);
        throw err;
      }
      if (!data._finishCalled) {
        data.finish();
      }
    });
  }
  return EventEmitter.addListener(event, callback);
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

FCM.setNotificationCategories = (categories) => {
  if (Platform.OS === 'ios') {
    RNFIRMessaging.setNotificationCategories(categories);
  }
}

export default FCM;

export {};
