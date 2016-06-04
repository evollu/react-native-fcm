'use strict';

var React = require('react-native');
var {NativeModules} = React;

var FIRMessaging = NativeModules.RNFIRMessaging;

class FCM {

    static getFCMToken() {
        return FIRMessaging.getFCMToken();
    }

    static requestPermissions() {
        return FIRMessaging.requestPermissions();
    }

}

FCM.initialData = FIRMessaging.initialData;
FCM.initialAction = FIRMessaging.initialAction;

module.exports = FCM;