import { Platform, AsyncStorage, AppState } from 'react-native';

import firebase from 'react-native-firebase';

AsyncStorage.getItem('lastNotification').then(data=>{
  if(data){
    // if notification arrives when app is killed, it should still be logged here
    console.log('last notification', JSON.parse(data));
    AsyncStorage.removeItem('lastNotification');
  }
})

export function registerKilledListener(message: RemoteMessage){
  AsyncStorage.setItem('lastNotification', JSON.stringify(message));
}

// these callback will be triggered only when app is foreground or background
export function registerAppListener(navigation){
  this.notificationOpenedListener = firebase.notifications().onNotificationOpened((notificationOpen: NotificationOpen) => {
    const notif: Notification = notificationOpen.notification;

    if(notif.targetScreen === 'detail'){
      setTimeout(()=>{
        navigation.navigate('Detail')
      }, 500)
    }
    setTimeout(()=>{
      alert(`User tapped notification\n${JSON.stringify(notif)}`)
    }, 500)
  });

  this.onTokenRefreshListener = firebase.messaging().onTokenRefresh(token => {
    console.log("TOKEN (refreshUnsubscribe)", token);
  });

  this.messageListener = firebase.messaging().onMessage((message: RemoteMessage) => {
    // Process your message as required
  });

}

// FCM.setNotificationCategories([
//   {
//     id: 'com.myidentifi.fcm.text',
//     actions: [
//       {
//         type: NotificationActionType.TextInput,
//         id: 'reply',
//         title: 'Quick Reply',
//         textInputButtonTitle: 'Send',
//         textInputPlaceholder: 'Say something',
//         intentIdentifiers: [],
//         options: NotificationActionOption.AuthenticationRequired
//       },
//       {
//         type: NotificationActionType.Default,
//         id: 'view',
//         title: 'View in App',
//         intentIdentifiers: [],
//         options: NotificationActionOption.Foreground
//       },
//       {
//         type: NotificationActionType.Default,
//         id: 'dismiss',
//         title: 'Dismiss',
//         intentIdentifiers: [],
//         options: NotificationActionOption.Destructive
//       }
//     ],
//     options: [NotificationCategoryOption.CustomDismissAction, NotificationCategoryOption.PreviewsShowTitle]
//   }
// ])
