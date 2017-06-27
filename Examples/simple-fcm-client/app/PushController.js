import React, { Component } from 'react'

import { Platform } from 'react-native'

import FCM, { FCMEvent, RemoteNotificationResult, WillPresentNotificationResult, NotificationType } from 'react-native-fcm'

import firebaseClient from './FirebaseClient'

export default class PushController extends Component {
  constructor (props) {
    super(props)
  }
  // https://firebase.google.com/docs/cloud-messaging/js/client
  componentDidMount () {
    FCM.requestPermissions()

    FCM.getFCMToken().then(token => {
      console.log('TOKEN (getFCMToken)', token)
      this.props.onChangeToken(token)
    })

    if (Platform.OS === 'ios') {
      FCM.getAPNSToken().then(token => {
        console.log('APNS TOKEN (getFCMToken)', token)
      })
    }

    FCM.getInitialNotification().then(notif => {
      console.log('INITIAL NOTIFICATION', notif)
    })

    this.notificationListener = FCM.on(FCMEvent.Notification, notif => {
      console.log('Notification', notif)

      if (!notif.local_notification) {
        console.log('not local')
        FCM.presentLocalNotification({
        notif})
      }

      if (Platform.OS === 'ios') {
        // optional
        // iOS requires developers to call completionHandler to end notification process. If you do not call it your background remote notifications could be throttled, to read more about it see the above documentation link.
        // This library handles it for you automatically with default behavior (for remote notification, finish with NoData; for WillPresent, finish depend on "show_in_foreground"). However if you want to return different result, follow the following code to override
        // notif._notificationType is available for iOS platfrom
        switch (notif._notificationType) {
          case NotificationType.Remote:
            notif.finish(RemoteNotificationResult.NewData) // other types available: RemoteNotificationResult.NewData, RemoteNotificationResult.ResultFailed
            break
          case NotificationType.NotificationResponse:
            notif.finish()
            break
          case NotificationType.WillPresent:
            notif.finish(WillPresentNotificationResult.All) // other types available: WillPresentNotificationResult.None
            break
        }
      }
      // https://github.com/firebase/quickstart-js/blob/master/messaging/index.html
      // https://firebase.google.com/docs/admin/setup
      // http://docs.aws.amazon.com/sns/latest/dg/mobile-push-send-custommessage.html
      // http://docs.aws.amazon.com/cognito/latest/developerguide/push-sync.html
      this.refreshTokenListener = FCM.on(FCMEvent.RefreshToken, token => {
        console.log('TOKEN (refreshUnsubscribe)', token)
        FCM.getToken()
          .then(function (refreshedToken) {
            console.log('Token refreshed.')
            // Indicate that the new Instance ID token has not yet been sent to the
            // app server.
            setTokenSentToServer(false)
            // Send Instance ID token to app server.
            sendTokenToServer(refreshedToken)
          // ...
          })
          .catch(function (err) {
            console.log('Unable to retrieve refreshed token ', err)
            showToken('Unable to retrieve refreshed token ', err)
          })
        this.props.onChangeToken(token)
      })

      // direct channel related methods are ios only
      // directly channel is truned off in iOS by default, this method enables it
      FCM.enableDirectChannel()
      this.channelConnectionListener = FCM.on(FCMEvent.DirectChannelConnectionChanged, (data) => {
        console.log('direct channel connected' + data)
      })
      setTimeout(function () {
        FCM.isDirectChannelEstablished().then(d => console.log(d))
      }, 1000)
    })
  }

  componentWillUnmount () {
    this.notificationListener.remove()
    this.refreshTokenListener.remove()
  }

  render () {
    return null
  }
}
