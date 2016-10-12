import FirebaseConstants from "./FirebaseConstants";

const API_URL = "https://fcm.googleapis.com/fcm/send";

class FirebaseClient {

  sendNotification() {
    let body = {
    	"to": FirebaseConstants.TO,
    	"notification":{
    		"icon": "appLogo",
    		"title": "Notification Title",
    		"body": "Notification Body",
    		"sound": "default",
    		"click_action": "fcm.ACTION.HELLO"
    	},
    	"priority": 10
    }

    this._send(JSON.stringify(body), "notification");
  }

  sendData() {
    let body = {
    	"to": FirebaseConstants.TO,
      "data":{
    		"icon": "appLogo",
    		"title": "Notification Title",
    		"body": "Notification Body",
    		"sound": "default",
    		"click_action": "fcm.ACTION.HELLO"
    	},
    	"priority": 10
    }

    this._send(JSON.stringify(body), "data");
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
