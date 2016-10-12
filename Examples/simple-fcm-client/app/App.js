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
  View
} from 'react-native';

import PushController from "./PushController";
import firebaseClient from  "./FirebaseClient";

export default class App extends Component {
  render() {
    return (
      <View style={styles.container}>
        <PushController />
        <Text style={styles.welcome}>
          Welcome to Simple Fcm Client!
        </Text>

        <TouchableOpacity onPress={() => firebaseClient.sendNotification()} style={styles.button}>
          <Text style={styles.buttonText}>Send Notification</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => firebaseClient.sendData()} style={styles.button}>
          <Text style={styles.buttonText}>Send Data</Text>
        </TouchableOpacity>
      </View>
    );
  }
}

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
    marginBottom: 5,
  },
  button: {
    backgroundColor: "teal",
    paddingHorizontal: 20,
    paddingVertical: 10,
    marginVertical: 5,
    borderRadius: 10
  },
  buttonText: {
    color: "white",
    backgroundColor: "transparent"
  },
});
