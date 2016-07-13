RELEASE NOTE MOVED TO [Release Section](https://github.com/evollu/react-native-fcm/releases)

### 1.0.13 BREAKING CHANGES
- get initial intent inside module, support rn 0.29.0 (for people upgrading from older version, change `new FIRMessagingPackage(getIntent())` back to `new FIRMessagingPackage()`)
- remove initAction as it is just duplication of initData

### 1.0.12
DON'T USE

### 1.0.11
- change android library version to use 9.+ instead of 9.0.1 so it lives well with other libraries

### 1.0.10
- added support for projects not using cocoapods

### 1.0.9
- added FCM.on support
- returns string token instead of object for FCM.on('refreshToken') event 
- added support for subscribe/unsubscribe topic feature
