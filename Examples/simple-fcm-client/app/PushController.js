import React, { Component } from "react";

import FCM from "react-native-fcm";

import firebaseClient from  "./FirebaseClient";

export default class PushController extends Component {
  constructor(props) {
    super(props);
  }

  componentDidMount() {
    FCM.requestPermissions();

    FCM.getFCMToken().then(token => {
      console.log("TOKEN (getFCMToken)", token);
      this.props.onChangeToken(token);
    });

    FCM.getInitialNotification().then(notif => {
      console.log("INITIAL NOTIFICATION", notif)
    });

    this.notificationUnsubscribe = FCM.on("notification", notif => {
      console.log("Notification", notif);
    });

    this.refreshUnsubscribe = FCM.on("refreshToken", token => {
      console.log("TOKEN (refreshUnsubscribe)", token);
      this.props.onChangeToken(token);
    });
  }

  componentWillUnmount() {
    this.refreshUnsubscribe();
    this.notificationUnsubscribe();
  }


  render() {
    return null;
  }
}
