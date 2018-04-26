/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  StyleSheet,
  Text,
  TouchableOpacity,
  View,
  Clipboard,
  Platform,
  ScrollView,
  AsyncStorage
} from 'react-native';

import { StackNavigator } from 'react-navigation';

import firebase from 'react-native-firebase';

import {registerAppListener} from "./Listeners";
import firebaseClient from  "./FirebaseClient";

class MainPage extends Component {
  constructor(props) {
    super(props);

    this.state = {
      token: "",
      tokenCopyFeedback: ""
    }
  }

  async componentDidMount(){
    // Build a channel
    const channel = new firebase.notifications.Android.Channel('test-channel', 'Test Channel', firebase.notifications.Android.Importance.Max)
    .setDescription('My apps test channel');

    // Create the channel
    firebase.notifications().android.createChannel(channel);

    registerAppListener(this.props.navigation);
    firebase.notifications().getInitialNotification()
      .then((notificationOpen: NotificationOpen) => {
        if (notificationOpen) {
          // Get information about the notification that was opened
          const notif: Notification = notificationOpen.notification;
          this.setState({
            initNotif: notif.data
          })
          if(notif && notif.targetScreen === 'detail'){
            setTimeout(()=>{
              this.props.navigation.navigate('Detail')
            }, 500)
          }
        }
      });

    if (!await firebase.messaging().hasPermission()) {
      try {
        await firebase.messaging().requestPermission();
      } catch(e) {
        alert("Failed to grant permission")
      }
    }

    firebase.messaging().getToken().then(token => {
      console.log("TOKEN (getFCMToken)", token);
      this.setState({token: token || ""})
    });

    // topic example
    firebase.messaging().subscribeToTopic('sometopic');
    firebase.messaging().unsubscribeFromTopic('sometopic');

    var offline = await AsyncStorage.getItem('headless')
    if(offline){
      this.setState({
        offlineNotif: offline
      });
      AsyncStorage.removeItem('headless');
    }
  }

  componentWillUnmount(){
    this.onTokenRefreshListener();
    this.notificationOpenedListener();
    this.messageListener();
  }

  showLocalNotification() {
    let notification = new firebase.notifications.Notification();
    notification = notification.setNotificationId(new Date().valueOf().toString())
    .setTitle( "Test Notification with action")
    .setBody("Force touch to reply")
    .setSound("bell.mp3")
    .setData({
      now: new Date().toISOString()
    });
    notification.ios.badge = 10
    notification.android.setAutoCancel(true);

    notification.android.setBigPicture("https://www.google.com/images/branding/googlelogo/2x/googlelogo_color_120x44dp.png", "https://image.freepik.com/free-icon/small-boy-cartoon_318-38077.jpg", "content title", "summary text")
    notification.android.setColor("red")
    notification.android.setColorized(true)
    notification.android.setOngoing(true)
    notification.android.setPriority(firebase.notifications.Android.Priority.High)
    notification.android.setSmallIcon("ic_launcher")
    notification.android.setVibrate([300])
    notification.android.addAction(new firebase.notifications.Android.Action("view", "ic_launcher", "VIEW"))
    notification.android.addAction(new firebase.notifications.Android.Action("reply", "ic_launcher", "REPLY").addRemoteInput(new firebase.notifications.Android.RemoteInput("input")) )
    notification.android.setChannelId("test-channel")

    firebase.notifications().displayNotification(notification)
  }

  scheduleLocalNotification() {
    let notification = new firebase.notifications.Notification();
    notification = notification.setNotificationId(new Date().valueOf().toString())
    .setTitle( "Test Notification with action")
    .setBody("Force touch to reply")
    .setSound("bell.mp3")
    .setData({
      now: new Date().toISOString()
    });
    notification.android.setChannelId("test-channel")
    notification.android.setPriority(firebase.notifications.Android.Priority.High)
    notification.android.setSmallIcon("ic_launcher")

    firebase.notifications().scheduleNotification(notification, { fireDate: new Date().getTime() + 5000 })
  }

  sendRemoteNotification(token) {
    let body;

    if(Platform.OS === 'android'){
      body = {
        "to": token,
      	"data":{
					"title": "Simple FCM Client",
          "body": "Click me to go to detail",
          targetScreen: 'detail',
          now: new Date().toISOString()
    		},
    		"priority": 10
      }
    } else {
			body = {
				"to": token,
				"notification":{
					"title": "Simple FCM Client",
					"body": "Click me to go to detail",
					"sound": "default"
        },
        data: {
          targetScreen: 'detail',
          now: new Date().toISOString()
        },
				"priority": 10
			}
		}

    firebaseClient.send(JSON.stringify(body), "notification");
  }

  render() {
    let { token, tokenCopyFeedback } = this.state;

    return (
      <View style={styles.container}>
      <ScrollView style={{paddingHorizontal: 20}}>
        <Text style={styles.welcome}>
          Welcome to Simple Fcm Client!
        </Text>

        <Text style={styles.feedback}>
          {this.state.tokenCopyFeedback}
        </Text>

        <Text style={styles.feedback}>
          Remote notif won't be available to iOS emulators
        </Text>

        <TouchableOpacity onPress={() => this.sendRemoteNotification(token)} style={styles.button}>
          <Text style={styles.buttonText}>Send Remote Notification</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => this.showLocalNotification()} style={styles.button}>
          <Text style={styles.buttonText}>Show Local Notification</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => this.scheduleLocalNotification()} style={styles.button}>
          <Text style={styles.buttonText}>Schedule Notification in 5s</Text>
        </TouchableOpacity>

        <Text style={styles.instructions}>
          Init notif:
        </Text>
        <Text>
          {JSON.stringify(this.state.initNotif)}
        </Text>

        <Text style={styles.instructions}>
          Notif when app was closed:
        </Text>
        <Text>
          {this.state.offlineNotif}
        </Text>

        <Text style={styles.instructions}>
          Token:
        </Text>
        <Text selectable={true} onPress={() => this.setClipboardContent(this.state.token)}>
          {this.state.token}
        </Text>
        </ScrollView>
      </View>
    );
  }

  setClipboardContent(text) {
    Clipboard.setString(text);
    this.setState({tokenCopyFeedback: "Token copied to clipboard."});
    setTimeout(() => {this.clearTokenCopyFeedback()}, 2000);
  }

  clearTokenCopyFeedback() {
    this.setState({tokenCopyFeedback: ""});
  }
}

class DetailPage extends Component {
  render(){
    return <View style={{ flex: 1, alignItems: 'center', justifyContent: 'center' }}>
      <Text>Detail page</Text>
    </View>
  }
}

export default StackNavigator({
  Main: {
    screen: MainPage,
  },
  Detail: {
    screen: DetailPage
  }
}, {
  initialRouteName: 'Main',
});

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  instructions: {
    textAlign: 'center',
    color: '#333333',
    marginBottom: 2,
  },
  feedback: {
    textAlign: 'center',
    color: '#996633',
    marginBottom: 3,
  },
  button: {
    backgroundColor: "teal",
    paddingHorizontal: 20,
    paddingVertical: 15,
    marginVertical: 10,
    borderRadius: 10
  },
  buttonText: {
    color: "white",
    backgroundColor: "transparent"
  },
});
