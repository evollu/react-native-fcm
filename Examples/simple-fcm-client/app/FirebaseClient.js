import FirebaseConstants from './FirebaseConstants'
// import * as admin from 'firebase-admin'

// const API_URL = 'https://fcm.googleapis.com/fcm/send'

// defaultApp = admin.initializeApp({
//   credential: admin.credential.cert({
//     projectId: 'fir-thing-2029b',
//     clientEmail: 'pcgcloud@gmail.com',
//     privateKey: '-----BEGIN PRIVATE KEY-----\nAIzaSyDWB9rvgmKT_RWQyMB2gVEMYURByJPj6f4\n-----END PRIVATE KEY-----\n'
//   }),
//   databaseURL: 'gs://fir-thing-2029b.appspot.com'
// })

// console.log(defaultApp.name); // "[DEFAULT]"

// // Retrieve services via the defaultApp variable...
// var defaultAuth = defaultApp.auth()
// var defaultDatabase = defaultApp.database()

// defaultAuth.auth().getUserByEmail('e.meek.s@pcgus.com')
//   .then(function (userRecord) {
//     // See the UserRecord reference doc for the contents of userRecord.
//     console.log('Successfully fetched user data:', userRecord.toJSON())
//   })
//   .catch(function (error) {
//     console.log('Error fetching user data:', error)
//   })

class FirebaseClient {

  constructor () {
    this.sendData = this.sendData.bind(this)
    this.sendNotification = this.sendNotification.bind(this)
    this.sendNotificationWithData = this.sendNotificationWithData.bind(this)
  }

  sendNotification (token) {
    let body = {
      'to': token,
      'notification': {
        'title': 'Simple FCM Client',
        'body': 'This is a notification with only NOTIFICATION.',
        'sound': 'default',
        'click_action': 'fcm.ACTION.HELLO'
      },
      'priority': 10
    }

    this._send(JSON.stringify(body), 'notification')
  }

  sendData (token) {
    let body = {
      'to': token,
      'data': {
        'title': 'Simple FCM Client',
        'body': 'This is a notification with only DATA.',
        'sound': 'default',
        'click_action': 'fcm.ACTION.HELLO',
        'remote': true
      },
      'priority': 'normal'
    }

    this._send(JSON.stringify(body), 'data')
  }

  sendNotificationWithData (token) {
    let body = {
      'to': token,
      'notification': {
        'title': 'Simple FCM Client',
        'body': 'This is a notification with NOTIFICATION and DATA (NOTIF).',
        'sound': 'default',
        'click_action': 'fcm.ACTION.HELLO'
      },
      'data': {
        'title': 'Simple FCM Client',
        'body': 'This is a notification with NOTIFICATION and DATA (DATA)',
        'click_action': 'fcm.ACTION.HELLO',
        'remote': true
      },
      'priority': 'high'
    }

    this._send(JSON.stringify(body), 'notification-data')
  }

  _send (body, type) {
    let headers = new Headers({
      'Content-Type': 'application/json',
      'Content-Length': parseInt(body.length),
      'Authorization': 'key=' + FirebaseConstants.KEY
    })

    fetch(API_URL, { method: 'POST', headers, body})
      .then(response => console.log('Send ' + type + ' response', response))
      .catch(error => console.log('Error sending ' + type, error))
  }

}

let firebaseClient = new FirebaseClient()
export default firebaseClient
