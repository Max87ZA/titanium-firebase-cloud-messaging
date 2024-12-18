# Firebase Cloud Messaging - Titanium Module

Use the native Firebase SDK (iOS/Android) in Axway Titanium. This repository is part of the [Titanium Firebase](https://github.com/hansemannn/titanium-firebase) project.

## Supporting this effort

The whole Firebase support in Titanium is developed and maintained by the community (`@hansemannn` and `@m1ga`). To keep
this project maintained and be able to use the latest Firebase SDK's, please see the "Sponsor" button of this repository,
thank you!

## Topics
* [Requirements](#requirements)
* [Download](#download)
* [iOS notes](#ios-notes)
* [Android Notes](#android-notes)
* [API: Methods, Properties, Events](#api)
* [Example](#example)
* [Sending push messages](#sending-push-messages)
* [Build from source](#build)

## Requirements
- [x] iOS: [Firebase-Core](https://github.com/hansemannn/titanium-firebase-core)
- [x] iOS: Titanium SDK 10.0.0
- [x] Android: Titanium SDK 9.0.0+, [Ti.PlayServices](https://github.com/appcelerator-modules/ti.playservices) module
- [x] Read the [Titanium-Firebase](https://github.com/hansemannn/titanium-firebase#installation) install part if you set up a new project.


## Download
- [x] [Stable release](https://github.com/hansemannn/titanium-firebase-cloud-messaging/releases)
- [x] [![gitTio](http://hans-knoechel.de/shields/shield-gittio.svg)](http://gitt.io/component/firebase.cloudmessaging)

## iOS notes:

<table>
<tr>
<td style="vertical-align:top;">
<img src="example/ios_push1.jpg"/>
</td>
<td style="vertical-align:top;">
<img src="example/ios_push2.jpg"/>
</td>
</tr>
</table>

To register for push notifications on iOS, you only need to call the Titanium related methods as the following:
```js
// Listen to the notification settings event
Ti.App.iOS.addEventListener('usernotificationsettings', function eventUserNotificationSettings() {
  // Remove the event again to prevent duplicate calls through the Firebase API
  Ti.App.iOS.removeEventListener('usernotificationsettings', eventUserNotificationSettings);

  // Register for push notifications
  Ti.Network.registerForPushNotifications({
    success: function () { ... },
    error: function () { ... },
    callback: function () { ... } // Fired for all kind of notifications (foreground, background & closed)
  });
});

// Register for the notification settings event
Ti.App.iOS.registerUserNotificationSettings({
  types: [
    Ti.App.iOS.USER_NOTIFICATION_TYPE_ALERT,
    Ti.App.iOS.USER_NOTIFICATION_TYPE_SOUND,
    Ti.App.iOS.USER_NOTIFICATION_TYPE_BADGE
  ]
});
```

## Android Notes:

<table>
<tr>
<td style="vertical-align:top;">
<img src="example/android_big_image.png" valign="top"/>
</td>
<td style="vertical-align:top;">
<img src="example/android_big_text.png"/>
</td>
</tr>
<tr><td>
Big image notification with colored icon/appname
</td>
<td>
Big text notification with colored icon/appname
</td>
</tr>
</table>

### Register for push

If you use Titanium 12.0.0+ you can use
```js
Ti.Network.registerForPushNotifications({
  success: function () { ... },
  error: function () { ... }
});
```

to request Android 13 runtime permissions. All other version < Android 13 will call the `success` function right away.

If you have runtime permissions (the `success` event mentioned above or `Ti.Network.remoteNotificationsEnabled` is true) you can call `fcm.registerForPushNotifications()` to request a token. Check the full example below for all steps.

### Android 13 permission
If you use **Titanium >=12.0.0** (target SDK 33) you can use `Ti.Network.registerForPushNotifications()` to ask for the permission.
You can also request the permission with older SDKs yourself by using the general `requestPermissions()` method:
```js
var permissions = ['android.permission.POST_NOTIFICATIONS'];
Ti.Android.requestPermissions(permissions, function(e) {
  if (e.success) {
    Ti.API.info('SUCCESS');
  } else {
    Ti.API.info('ERROR: ' + e.error);
});
```

### Setting the Notification Icon

For a `data notification` you have to place a notification icon "notificationicon.png" into the following folder:
 `[application_name]/[app*]/platform/android/res/drawable/`
 or
 `[application_name]/[app*]/platform/android/res/drawable-*` (if you use custom dpi folders)

<small>**\*** = Alloy</small>

To use the custom icon for a `notification message` you need to add this attribute within the `<application/>` section of your `tiapp.xml`:

```xml
<meta-data android:name="com.google.firebase.messaging.default_notification_icon" android:resource="@drawable/notificationicon"/>
```

Otherwise the default icon will be used.

It should be flat (no gradients), white and face-on perspective and have a transparent background. The icon will only show the outline/shape of your icon so make sure all you e.g. white is transparent otherwise it will just be a square.

> **Note**: You should generate the icon for all resolutions.

```
22 × 22 area in 24 × 24 (mdpi)
33 × 33 area in 36 × 36 (hdpi)
44 × 44 area in 48 × 48 (xhdpi)
66 × 66 area in 72 × 72 (xxhdpi)
88 × 88 area in 96 × 96 (xxxhdpi)
```

You can use this script to generate it **once you put** the icon in `drawable-xxxhdpi/notificationicon.png` and have
Image Magick installed. On macOS, you can install it using `brew install imagemagick`, on Windows you can download it [here](https://imagemagick.org/script/download.php).

```sh
#!/bin/sh

ICON_SOURCE="app/platform/android/res/drawable-xxxhdpi/notificationicon.png"
if [ -f "$ICON_SOURCE" ]; then
    mkdir -p "app/platform/android/res/drawable-xxhdpi"
    mkdir -p "app/platform/android/res/drawable-xhdpi"
    mkdir -p "app/platform/android/res/drawable-hdpi"
    mkdir -p "app/platform/android/res/drawable-mdpi"
    convert "$ICON_SOURCE" -resize 72x72 "app/platform/android/res/drawable-xxhdpi/notificationicon.png"
    convert "$ICON_SOURCE" -resize 48x48 "app/platform/android/res/drawable-xhdpi/notificationicon.png"
    convert "$ICON_SOURCE" -resize 36x36 "app/platform/android/res/drawable-hdpi/notificationicon.png"
    convert "$ICON_SOURCE" -resize 24x24 "app/platform/android/res/drawable-mdpi/notificationicon.png"
else
    echo "No 'notificationicon.png' file found in app/platform/android/res/drawable-xxxhdpi"
fi
```

### Data / Notification messages

On Android there are two different messages that the phone can process: `Notification messages` and `Data messages`. A `Notification message` is processed by the system, the `Data message` is handeled by `showNotification()` in `TiFirebaseMessagingService`. Using the `notification` block inside the POSTFIELDS will send a `Notification message`.

Supported data fields:
* "title" => "string"
* "message" => "string"
* "big_text" => "string"
* "big_text_summary" => "string"
* "icon" => "Remote URL"
* "image" => "Remote URL"
* "rounded_large_icon" => "Boolean" (to display the largeIcon as a rounded image when the icon field is present)
* "force_show_in_foreground" => "Boolean" (show notification even app is in foreground)
* "id" => "int"
* "color" => will tint the app name and the small icon next to it
* "vibrate" => "boolean"
* "sound" => "string" (e.g. "notification.mp3" will play /platform/android/res/raw/notification.mp3)
* "badge" => "int" (if supported by the phone it will show a badge with this number)

Supported notification fields:
* "title" => "string"
* "body" => "string"
* "color" => "#00ff00",
* "tag" => "custom_notification_tag",   // push with the same tag will replace each other
* "sound" => "string" (e.g. "notification.mp3" will play /platform/android/res/raw/notification.mp3)

### Android: Note about custom sounds
To use a custom sound you have to create a second channel. The default channel will always use the default notification sound on the device!
If you send a normal or mixed notification you have to set the `android_channel_id` in the `notification` node. If you send a data notification the key is called `channelId`. Chech <a href="#extended-php-android-example">extended PHP Android example</a> for a PHP example.

#### Android: Note for switching between v<=v2.0.2 and >=v2.0.3 if you use notification channels with custom sounds
With versions prior to 2.0.3 of this module, FirebaseCloudMessaging.createNotificationChannel would create the notification sound uri using the resource id of the sound file in the `res/raw` directory. However, as described in this [android issue](https://issuetracker.google.com/issues/131303134), those resource ids can change to reference different files (or no file) between app versions, and  that happens the notification channel may play a different or no sound than originally intended.
With version 2.0.3 and later, we now create the uri's using the string filename so that it will not change if resource ids change. So if you are on version <=2.0.2 and are switching to version >=2.0.3, you will want to check if this is a problem for you by installing a test app using version >= 2.0.3 as an upgrade to a previous test app using version <= 2.0.2. Note that you should not uninstall the first app before installing the second app; nor should you reset user data.
If it is a problem you can workaround by first deleting the existing channel using deleteNotificationChannel, and then recreating the channel with the same settings as before, except with a different id. Don't forget that your push server will need to be version aware and send to this new channel for newer versions of your apps.

### Errors with firebase.analytics

If you run into errors in combination with firebase.analytics e.g. `Error: Attempt to invoke virtual method 'getInstanceId()' on a null object reference` you can add:

```xml
<service android:name="com.google.firebase.components.ComponentDiscoveryService" >
	<meta-data android:name="com.google.firebase.components:com.google.firebase.iid.Registrar"
		android:value="com.google.firebase.components.ComponentRegistrar" />
</service>
```
to the tiapp.xml

## API

### `FirebaseCloudMessaging`

#### Methods

`registerForPushNotifications()`

`appDidReceiveMessage(parameters)` (iOS only)
  - `parameters` (Object)

Note: Only call this method if method swizzling is disabled (enabled by default). Messages are received via the native delegates instead,
so receive the `gcm.message_id` key from the notification payload instead.

`sendMessage(parameters)`
  - `parameters` (Object)
    - `messageID` (String)
    - `to` (String)
    - `timeToLive` (Number)
    - `data` (Object)

`subscribeToTopic(topic)`
  - `topic` (String)

`unsubscribeFromTopic(topic)`
  - `topic` (String)

`setNotificationChannel(channel)` - Android-only
  - `channel` (NotificationChannel Object) Use `Ti.Android.NotificationManager.createNotificationChannel()` to create the channel and pass it to the function. See [Titanium.Android.NotificationChannel](https://docs.appcelerator.com/platform/latest/#!/api/Titanium.Android.NotificationChannel)

  _Prefered way_ to set a channel. As an alternative you can use `createNotificationChannel()`

`createNotificationChannel(parameters)` - Android-only

- `parameters` (Object)
  - `sound` (String) optional, refers to a sound file (without extension) at `platform/android/res/raw`. If sound == "default" or not passed in, will use the default sound. If sound == "silent" the channel will have no sound
  - `channelId` (String) optional, defaults to "default"
  - `channelName` (String) optional, defaults to `channelId`
  - `importance` (String) optional, either "low", "high", "default". Defaults to "default", unless sound == "silent", then defaults to "low".
  - `lights` (Boolean) optional, defaults to `false`
  - `showBadge` (Boolean) optional, defaults to `false`

  Read more in the [official Android docs](https://developer.android.com/reference/android/app/NotificationChannel).

`deleteNotificationChannel(channelId)` - Android-only
  - `channelId` (String) - same as the id used to create in createNotificationChannel

`setForceShowInForeground(showInForeground)` - Android-only
  - `showInForeground` (Boolean) Force the notifications to be shown in foreground.

`clearLastData()` - Android-only
  - Will empty the stored lastData values.

`getToken()` - Android-only
  - Returns the current FCM token.

`deleteToken()` - Android-only
  - Removes the current FCM token.



#### Properties

`shouldEstablishDirectChannel` (Number, get/set)

`fcmToken` (String, get)

`apnsToken` (String, set) (iOS only)

`lastData` (Object) (Android only)
The propery `lastData` will contain the data part when you send a notification push message (so both nodes are visible inside the push payload). Read before calling `registerForPushNotifications()`.

#### Events

`didReceiveMessage`
  - `message` (Object)

	iOS Note: This method is only called on iOS 10+ and only for direct messages sent by Firebase. Normal Firebase push notifications
	are still delivered via the Titanium notification events, e.g.
	```js
	Ti.App.iOS.addEventListener('notification', function(event) {
	  // Handle foreground notification
	});

	Ti.App.iOS.addEventListener('remotenotificationaction', function(event) {
	  // Handle background notification action click
	});
	```

`didRefreshRegistrationToken`
  - `fcmToken` (String)

`success` (Android only)
  - will fire on Android 13 after you call `registerForPushNotifications` to allow Push notifications

`error` (Android only)
  - `error` (String): Error during token registration or user denied `registerForPushNotifications`

`subscribe` (Android only)
  - `success` (Boolean): Successfully subscribed

`unsubscribe` (Android only)
  - `success` (Boolean): Successfully unsubscribed

`tokenRemoved` (Android only)
  - `success` (Boolean): Successfully removed token

## Example

```js
if (OS_IOS) {
	const FirebaseCore = require('firebase.core');
	FirebaseCore.configure();
}

// Important: The cloud messaging module has to imported after (!) the configure()
// method of the core module is called
const FirebaseCloudMessaging = require('firebase.cloudmessaging');

// Called when the Firebase token is registered or refreshed.
FirebaseCloudMessaging.addEventListener('didRefreshRegistrationToken', onToken);

// Called when direct messages arrive. Note that these are different from push notifications.
FirebaseCloudMessaging.addEventListener('didReceiveMessage', function(e) {
	Ti.API.info('Message', e.message);
});


if (OS_ANDROID) {
	// Android

	// create a notification channel
	const channel = Ti.Android.NotificationManager.createNotificationChannel({
		id: 'default', // if you use a custom id you have to set the same to the `channelId` in you php send script!
		name: 'Default channel',
		importance: Ti.Android.IMPORTANCE_DEFAULT,
		enableLights: true,
		enableVibration: true,
		showBadge: true
	});
	FirebaseCloudMessaging.notificationChannel = channel;

	// display last push data if available
	Ti.API.info(`Last data: ${FirebaseCloudMessaging.lastData}`);

	// request push permission
	requestPushPermissions();
} else {
	// iOS
	// Listen to the notification settings event
	Ti.App.iOS.addEventListener('usernotificationsettings', function eventUserNotificationSettings() {
		// Remove the event again to prevent duplicate calls through the Firebase API
		Ti.App.iOS.removeEventListener('usernotificationsettings', eventUserNotificationSettings);
		requestPushPermissions();
	});

	// Register for the notification settings event
	Ti.App.iOS.registerUserNotificationSettings({
		types: [
			Ti.App.iOS.USER_NOTIFICATION_TYPE_ALERT,
			Ti.App.iOS.USER_NOTIFICATION_TYPE_SOUND,
			Ti.App.iOS.USER_NOTIFICATION_TYPE_BADGE
		]
	});
}

function requestPushPermissions() {
	// Register for push notifications
	Ti.Network.registerForPushNotifications({
		success: function(e) {
			// Register the device with the FCM service.
			if (OS_ANDROID) {
				// register for a token
				FirebaseCloudMessaging.registerForPushNotifications();
			} else {
				// iOS
				onToken(e);
			}
		},
		error: function(e) {
			Ti.API.error(e);
		},
		callback: function(e) {
			// Fired for all kind of notifications (foreground, background & closed)
			Ti.API.info(e.data);
		}
	});
}

function onToken(e) {
	// new device is registered

	if (OS_ANDROID) {
		Ti.API.info("New token", e.fcmToken);
	} else {
		if (FirebaseCloudMessaging != null) {
			Ti.API.info("New token", FirebaseCloudMessaging.fcmToken);
		}
	}
}

// Check if token is already available.
if (FirebaseCloudMessaging.fcmToken) {
	Ti.API.info('FCM-Token', FirebaseCloudMessaging.fcmToken);
} else {
	Ti.API.info('Token is empty. Waiting for the token callback ...');
}

// Subscribe to a topic.
FirebaseCloudMessaging.subscribeToTopic('testTopic');
```
### Android intent data

Example to get the the resume data/notification click data on Android:

```javascript
const handleNotificationData = (notifObj) => {
	if (notifObj) {
		notifData = JSON.parse(notifObj);
		// ...process notification data...
		FirebaseCloudMessaging.clearLastData();
	}
}

// Check if app was launched on notification click
const launchIntent = Ti.Android.rootActivity.intent;
handleNotificationData(launchIntent.getStringExtra("fcm_data"));

Ti.App.addEventListener('resumed', function() {
	// App was resumed from background on notification click
	const currIntent = Titanium.Android.currentActivity.intent;
	const notifData = currIntent.getStringExtra("fcm_data");
	handleNotificationData(notifData);
});
```

## Sending push messages

Check https://firebase.google.com/docs/cloud-messaging/server or frameworks like https://github.com/kreait/firebase-php/

### REST API
Firstly, you need to set up your service account via Firebase console, download json and upload it to your server. 

To generate your access token use the following PHP file example:

```php
<?php
$serviceAccountKeyPath = 'your_serviceAccount.json';

// Function to generate Bearer token using service account credentials
function generateAccessToken()
{
    $serviceAccountKeyPath = __DIR__ . '/your_serviceAccount.json';

    $serviceAccount = json_decode(file_get_contents($serviceAccountKeyPath), true);
    $privateKey = $serviceAccount['private_key'];
    $clientEmail = $serviceAccount['client_email'];

    $header = base64url_encode(json_encode([
        'alg' => 'RS256',
        'typ' => 'JWT',
    ]));

    $now = time();
    $payload = base64url_encode(json_encode([ 
        'iss' => $clientEmail,
        'scope' => 'https://www.googleapis.com/auth/firebase.messaging',
        'aud' => 'https://oauth2.googleapis.com/token',
        'iat' => $now,
        'exp' => $now + 3600,
    ]));

    $jwtToSign = $header . '.' . $payload;
    openssl_sign($jwtToSign, $signature, $privateKey, 'SHA256');
    $jwtSignature = base64url_encode($signature);

    $jwt = $jwtToSign . '.' . $jwtSignature;

    $curl = curl_init();
    curl_setopt_array($curl, [
        CURLOPT_URL => 'https://oauth2.googleapis.com/token',
        CURLOPT_RETURNTRANSFER => true,
        CURLOPT_POST => true,
        CURLOPT_POSTFIELDS => http_build_query([
            'grant_type' => 'urn:ietf:params:oauth:grant-type:jwt-bearer',
            'assertion' => $jwt,
        ]),
        CURLOPT_HTTPHEADER => ['Content-Type: application/x-www-form-urlencoded'],
    ]);

    $response = curl_exec($curl);
    curl_close($curl);

    $tokenData = json_decode($response, true);

    return $tokenData['access_token'];
}
function base64url_encode($data)
{
    return rtrim(strtr(base64_encode($data), '+/', '-_'), '=');
}
$accessToken = generateAccessToken();
echo $accessToken;
?>
```
Upload it to your server, same folder as your service_account.json file. Load that php and get authorization token.

Finally, you can send an API request like this:

url: POST, https://fcm.googleapis.com/v1/projects/your-project-name/messages:send

auth: Bearer token taken from PHP

body: 

```json
{
    "message":{
        "token": "dZ8W2DrrRcOEbm9T7Qjqgq:APA91bFlPOyVbzIfHR7RsNTW4VU6XoQId_NW1DjImZ88SFXE6lFr86W1irctENzWmoy8fwzOYvi_dFiRi3IbxalbMAY4lY6oJ9gMHZI1LgDjJJdeb6B_CwQ",
        "data": {
            "type": "type"
        },
        
        "apns":{
            "payload":{
                "aps": {
		    "title": "Title showed in Notification center",
                    "alert": "Alert in Notification center",
                    "badge": 1,
                    "sound":"push.caf", // put your caf into assets/sounds
                    "icon":"icon",
                    "custom_field": {
                        "type": "type" //your custom data
                    }
                }
            }
        },
		"android": 
		{
			"data":
			{
				"channelId": "your_notification_channel_name",
				"title": "Title showed in Notification center",
				"sound":"push",
				"body": "body", 
				"alert": "Alert in Notification center"
				"url_to_load": "titaniumsdk.com" //your custom data
			}
		}
    }
}
```
message.token - token from FCM module, notification is sent to one device/array of devices only


message.topic - subscribed topic name, notification is sent to all devices subscribed



## Parse

You can use Parse with this module: https://github.com/timanrebel/Parse/pull/59 in combination with Firebase. You include and configure both modules and send your deviceToken to the Parse backend.

If you send a push over e.g. <a href="https://sashido.io">Sashido</a> you can either send a normal text or a json with:
```json
{"alert":"test from sashido", "text":"test"}
```
With the JSON you can set a title/alert and the text of the notification.

## Build

### iOS

```bash
cd ios
ti build -p ios --build-only
```

### Android

```bash
cd android
ti build -p android --build-only
```

## Legal

(c) 2017-Present by Hans Knöchel & Michael Gangolf
