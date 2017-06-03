# 2017-05-08 -- v2.0.0
- Introduced an improved interface for Swift 3 developers
- Deprecated some methods and properties after moving their logic to the
  Firebase Cloud Messaging SDK
- Fixed an intermittent stability issue when a debug build of an app was
  replaced with a release build of the same version
- Removed swizzling logic that was sometimes resulting in developers receiving
  a validation notice about enabling push notification capabilities, even though
  they weren't using push notifications
- Fixed a notification that would sometimes fire twice in quick succession
  during the first run of an app

# 2017-03-31 -- v1.0.10

- Improvements to token-fetching logic
- Fixed some warnings in Instance ID
- Improved error messages if Instance ID couldn't be initialized properly
- Improvements to console logging

# 2017-01-31 -- v1.0.9

- Removed an error being mistakenly logged to the console.

# 2016-07-06 -- v1.0.8

- Don't store InstanceID plists in Documents folder.

# 2016-06-19 -- v1.0.7

- Fix remote-notifications warning on app submission.

# 2016-05-16 -- v1.0.6

- Fix CocoaPod linter issues for InstanceID pod.

# 2016-05-13 -- v1.0.5

- Fix Authorization errors for InstanceID tokens.

# 2016-05-11 -- v1.0.4

- Reduce wait for InstanceID token during parallel requests.

# 2016-04-18 -- v1.0.3

- Change flag to disable swizzling to *FirebaseAppDelegateProxyEnabled*.
- Fix incessant Keychain errors while accessing InstanceID.
- Fix max retries for fetching IID token.

# 2016-04-18 -- v1.0.2

- Register for remote notifications on iOS8+ in the SDK itself.
