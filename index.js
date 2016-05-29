'use strict';

var React = require('react-native');
var {NativeModules} = React;

var FIRMessaging = NativeModules.RNFIRMessaging;

console.log(NativeModules.RNFIRMessaging);

class FCM {

    static getFCMToken() {
        return FIRMessaging.getFCMToken();
    }

    static requestPermissions() {
        return FIRMessaging.requestPermissions();
    }

}

module.exports = FCM;