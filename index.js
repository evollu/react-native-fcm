import {NativeModules, DeviceEventEmitter, Platform} from 'react-native';

export const FCMEvent = {
  RefreshToken: 'FCMTokenRefreshed',
  Notification: 'FCMNotificationReceived'
}

export const RemoteNotificationResult = {
  NewData: 'UIBackgroundFetchResultNewData',
  NoData: 'UIBackgroundFetchResultNoData',
  ResultFailed: 'UIBackgroundFetchResultFailed'
}

export const WillPresentNotificationResult = {
  All: 'UNNotificationPresentationOptionAll',
  None: 'UNNotificationPresentationOptionNone'
}

export const NotificationType = {
  Remote: 'remote_notification',
  NotificationResponse: 'notification_response',
  WillPresent: 'will_present_notification',
  Local: 'local_notification'
}

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
    details.id = details.id || new Date().getTime().toString()
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
    if(!notificationID){
		return;
	}
	RNFIRMessaging.cancelLocalNotification(notificationID);
};

FCM.cancelAllLocalNotifications = () => {
    RNFIRMessaging.cancelAllLocalNotifications();
};

FCM.removeDeliveredNotification = (notificationID) => {
	if(!notificationID){
		return;
	}
	RNFIRMessaging.removeDeliveredNotification(notificationID);
}

FCM.removeAllDeliveredNotifications = () => {
	RNFIRMessaging.removeAllDeliveredNotifications();
}

FCM.setBadgeNumber = (number) => {
    RNFIRMessaging.setBadgeNumber(number);
}

FCM.getBadgeNumber = () => {
    return RNFIRMessaging.getBadgeNumber();
}


function finish(result){
  if(Platform.OS !== 'ios'){
    return;
  }
  if(!this._finishCalled && this._completionHandlerId){
    this._finishCalled = true;
    switch(this._notificationType){
      case NotificationType.Remote:
        result = result || RemoteNotificationResult.NoData;
        if(!Object.values(RemoteNotificationResult).includes(result)){
          throw new Error(`Invalid RemoteNotificationResult, use import {RemoteNotificationResult} from 'react-native-fcm' to avoid typo`);
        }
        RNFIRMessaging.finishRemoteNotification(this._completionHandlerId, result);
        return;
      case NotificationType.NotificationResponse:
        RNFIRMessaging.finishNotificationResponse(this._completionHandlerId);
        return;
      case NotificationType.WillPresent:
        result = result || (this.show_in_foreground ? WillPresentNotificationResult.All : WillPresentNotificationResult.None);
        if(!Object.values(WillPresentNotificationResult).includes(result)){
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

    if(event === FCMEvent.Notification){
      return DeviceEventEmitter.addListener(event, async(data)=>{
        data.finish = finish;
        try{
          await callback(data);
        } catch(err){
          console.error('Notification handler err', err)
          throw err;
        }
        if(!data._finishCalled){
          data.finish();
        }
      })
    }
    return DeviceEventEmitter.addListener(event, callback);
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

export default FCM;
