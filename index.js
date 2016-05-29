'use strict';

var React = require('react-native');
var {
    NativeModules
} = React;

var FCMModule = NativeModules.FCMModule;

class FCM {

    static getFCMToken() {
        return FCMModule.getFCMToken();
    }

    static requestPermissions() {
        return FCMModule.requestPermissions();
    }

    constructor(data) {
        this.data = data;
    }
}

module.exports = FCM;