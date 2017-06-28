import FirebaseConstants from './FirebaseConstants'
// import * as admin from 'firebase-admin'

const API_URL = 'https://fcm.googleapis.com/fcm/send'

// const defaultApp = admin.initializeApp({
//   credential: admin.credential.cert({
//     'project_info': {
//       'project_number': '724737413503',
//       'firebase_url': 'https://fir-thing-2029b.firebaseio.com',
//       'project_id': 'fir-thing-2029b',
//       'storage_bucket': 'fir-thing-2029b.appspot.com'
//     },
//     'client': [
//       {
//         'client_info': {
//           'mobilesdk_app_id': '1:724737413503:android:ddd9404b28cde600',
//           'android_client_info': {
//             'package_name': 'com.amazonaws.mobile'
//           }
//         },
//         'oauth_client': [
//           {
//             'client_id': '724737413503-cualrn5j5vqmko4ac9ear9e2sc0eo7jq.apps.googleusercontent.com',
//             'client_type': 3
//           }
//         ],
//         'api_key': [
//           {
//             'current_key': 'AIzaSyBM3w-Nm36dXWhTk9HKaIk378H1ywo6w2M'
//           }
//         ],
//         'services': {
//           'analytics_service': {
//             'status': 1
//           },
//           'appinvite_service': {
//             'status': 1,
//             'other_platform_oauth_client': []
//           },
//           'ads_service': {
//             'status': 2
//           }
//         }
//       },
//       {
//         'client_info': {
//           'mobilesdk_app_id': '1:724737413503:android:5920de3503a2d18c',
//           'android_client_info': {
//             'package_name': 'com.google.firebase.quickstart.fcm'
//           }
//         },
//         'oauth_client': [
//           {
//             'client_id': '724737413503-3va22siu8r2aad9s8kf1rrdi6bs4hdbn.apps.googleusercontent.com',
//             'client_type': 1,
//             'android_info': {
//               'package_name': 'com.google.firebase.quickstart.fcm',
//               'certificate_hash': '3c0d638f75d2ee82afd74a5377f4c2e532d34a6a'
//             }
//           },
//           {
//             'client_id': '724737413503-cualrn5j5vqmko4ac9ear9e2sc0eo7jq.apps.googleusercontent.com',
//             'client_type': 3
//           }
//         ],
//         'api_key': [
//           {
//             'current_key': 'AIzaSyBM3w-Nm36dXWhTk9HKaIk378H1ywo6w2M'
//           }
//         ],
//         'services': {
//           'analytics_service': {
//             'status': 1
//           },
//           'appinvite_service': {
//             'status': 2,
//             'other_platform_oauth_client': [
//               {
//                 'client_id': '724737413503-cualrn5j5vqmko4ac9ear9e2sc0eo7jq.apps.googleusercontent.com',
//                 'client_type': 3
//               }
//             ]
//           },
//           'ads_service': {
//             'status': 2
//           }
//         }
//       }
//     ],
//     'configuration_version': '1'
//   })
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
