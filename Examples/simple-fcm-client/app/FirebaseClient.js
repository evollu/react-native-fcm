import FirebaseConstants from "./FirebaseConstants";

const API_URL = "https://fcm.googleapis.com/fcm/send";

class FirebaseClient {

  constructor() {
    this.sendData = this.sendData.bind(this);
    this.sendNotification = this.sendNotification.bind(this);
    this.sendNotificationWithData = this.sendNotificationWithData.bind(this);
  }

  sendNotification(token) {
    let body = {
    	"to": token,
      "notification":{
    		"title": "Simple FCM Client",
    		"body": "This is a notification with only NOTIFICATION.",
    		"sound": "default",
    		"click_action": "fcm.ACTION.HELLO"
    	},
    	"priority": 10
    }

    this._send(JSON.stringify(body), "notification");
  }

  sendData(token) {
    let body = {
    	"to": token,
      "data":{
    		"title": "Simple FCM Client",
    		"body": "This is a notification with only DATA.",
    		"sound": "default",
    		"click_action": "fcm.ACTION.HELLO",
    		"remote": true
    	},
    	"priority": "normal"
    }

    this._send(JSON.stringify(body), "data");
  }

  sendNotificationWithData(token) {
    let body = {
      "to": token,
      "notification":{
    		"title": "Simple FCM Client",
    		"body": "This is a notification with NOTIFICATION and DATA (NOTIF).",
    		"sound": "default",
    		"click_action": "fcm.ACTION.HELLO"
    	},
    	"data":{
    		"title": "Simple FCM Client",
    		"body": "This is a notification with NOTIFICATION and DATA (DATA)",
    		"click_action": "fcm.ACTION.HELLO",
    		"remote": true
    	},
    	"priority": "high"
    }

    this._send(JSON.stringify(body), "notification-data");
  }

  _send(body, type) {
  	let headers = new Headers({
  		"Content-Type": "application/json",
  		"Content-Length": parseInt(body.length),
      "Authorization": "key=" + FirebaseConstants.KEY
  	});

  	fetch(API_URL, { method: "POST", headers, body })
  		.then(response => console.log("Send " + type + " response", response))
  		.catch(error => console.log("Error sending " + type, error));
  }

}

let firebaseClient = new FirebaseClient();
export default firebaseClient;
