/**
 * This file was auto-generated by the Titanium Module SDK helper for Android
 * Appcelerator Titanium Mobile
 * Copyright (c) 2009-2017 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Apache Public License
 * Please see the LICENSE included with this distribution for details.
 */
package firebase.cloudmessaging;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollModule;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.common.Log;
import org.appcelerator.titanium.TiApplication;
import org.appcelerator.titanium.TiBaseActivity;
import org.appcelerator.titanium.util.TiConvert;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ti.modules.titanium.android.notificationmanager.NotificationChannelProxy;

@Kroll.module(name = "CloudMessaging", id = "firebase.cloudmessaging")
public class CloudMessagingModule extends KrollModule {

    private static final String LCAT = "FirebaseCloudMessaging";
    private static final String FORCE_SHOW_IN_FOREGROUND = "titanium.firebase.cloudmessaging.key";
    private static CloudMessagingModule instance = null;
    private static String fcmToken = null;
    private String notificationData = "";

    public CloudMessagingModule() {
        super();
        instance = this;
    }

    @Kroll.onAppCreate
    public static void onAppCreate(TiApplication app) {
        // put module init code that needs to run when the application is created
    }

    public static CloudMessagingModule getInstance() {
        return instance;
    }

    // clang-format off
    @Kroll.method
    @Kroll.getProperty
    private KrollDict getLastData()
    // clang-format on
    {
        KrollDict data = new KrollDict();

        try {
            Intent intent = TiApplication.getAppRootOrCurrentActivity().getIntent();
            Bundle extras = intent.getExtras();

            if (extras != null) {
                for (String key : extras.keySet()) {
                    if (extras.get(key) instanceof Bundle) {
                        Bundle bndl = (Bundle) extras.get(key);
                        for (String bdnlKey : bndl.keySet()) {
                            data.put(key + "_" + bdnlKey, bndl.get(bdnlKey));
                        }
                    } else {
                        data.put(key, extras.get(key).toString());
                    }
                }

                data.put("inBackground", true);
            } else {
                Log.d(LCAT, "Empty extras in Intent");
                if (!notificationData.equals("")) {
                    data = new KrollDict(new JSONObject(notificationData));
                    data.put("inBackground", true);
                }
            }

            if (data.get("message") == null) {
                SharedPreferences preferences =
                        PreferenceManager.getDefaultSharedPreferences(Utils.getApplicationContext());
                String prefMessage = preferences.getString("titanium.firebase.cloudmessaging.message", null);
                if (prefMessage != null) {
                    data.put("message", new KrollDict(new JSONObject(prefMessage)));
                }
                preferences.edit().remove("titanium.firebase.cloudmessaging.message").apply();
            }
        } catch (Exception ex) {
            Log.e(LCAT, "getLastData" + ex);
        }

        return data;
    }

    // Methods
    @Kroll.method
    public void registerForPushNotifications() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (Utils.getApplicationContext().checkSelfPermission("android.permission.POST_NOTIFICATIONS") == PackageManager.PERMISSION_GRANTED) {
                fireEvent("success", new KrollDict());
                getToken();
            } else {
                Log.w(LCAT, "POST_NOTIFICATIONS runtime permission is missing. Please request that permission first.");
            }
        } else {
            fireEvent("success", new KrollDict());
            getToken();
        }
        parseBootIntent();
    }

    @Kroll.method
    public void subscribeToTopic(String topic) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                KrollDict data = new KrollDict();
                if (!task.isSuccessful()) {
                    data.put("success", false);
                } else {
                    data.put("success", true);
                }
                fireEvent("subscribe", data);
            }
        });
        Log.d(LCAT, "subscribe to " + topic);
    }

    @Kroll.method
    public void unsubscribeFromTopic(String topic) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                KrollDict data = new KrollDict();
                if (!task.isSuccessful()) {
                    data.put("success", false);
                } else {
                    data.put("success", true);
                }
                fireEvent("unsubscribe", data);
            }
        });
        Log.d(LCAT, "unsubscribe from " + topic);
    }

    @Kroll.method
    public void appDidReceiveMessage(KrollDict opt) {
        // empty
    }

    @Kroll.method
    public void clearLastData() {
        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(Utils.getApplicationContext());
        preferences.edit().remove("titanium.firebase.cloudmessaging.message").apply();

        // remove intent value
        Intent intent = TiApplication.getAppRootOrCurrentActivity().getIntent();
        String notification = intent.getStringExtra("fcm_data");
        if (notification != null) {
            intent.removeExtra("fcm_data");
        }
    }

    @Kroll.method
    public void getToken() {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (task.isSuccessful()) {
                    KrollDict data = new KrollDict();
                    fcmToken = task.getResult();
                    data.put("fcmToken", fcmToken);
                    fireEvent("didRefreshRegistrationToken", data);
                }
            }
        });
    }

    @Kroll.method
    public void deleteToken() {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();
        fm.deleteToken().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                KrollDict data = new KrollDict();
                if (!task.isSuccessful()) {
                    data.put("success", false);
                } else {
                    data.put("success", true);
                    fcmToken = null;
                }
                fireEvent("tokenRemoved", data);
            }
        });
    }

    @Kroll.method
    public void sendMessage(KrollDict obj) {
        FirebaseMessaging fm = FirebaseMessaging.getInstance();

        String fireTo = obj.getString("to");
        String fireMessageId = obj.getString("messageId");
        int ttl = TiConvert.toInt(obj.get("timeToLive"), 0);

        RemoteMessage.Builder rm = new RemoteMessage.Builder(fireTo);
        rm.setMessageId(fireMessageId);
        rm.setTtl(ttl);

        // add custom data
        Map<String, String> data = (HashMap) obj.get("data");
        assert data != null;
        for (Object o : data.keySet()) {
            rm.addData((String) o, data.get(o));
        }

        if (!fireTo.equals("") && !fireMessageId.equals("")) {
            fm.send(rm.build());
        } else {
            Log.e(LCAT, "Please set 'to' and 'messageId'");
        }
    }

    public void onTokenRefresh(String token) {
        try {
            if (hasListeners("didRefreshRegistrationToken")) {
                KrollDict data = new KrollDict();
                data.put("fcmToken", token);
                fireEvent("didRefreshRegistrationToken", data);
                fcmToken = token;
            }
        } catch (Exception e) {
            Log.e(LCAT, "Can't refresh token: " + e.getMessage());
        }
    }

    public void onMessageReceived(HashMap message) {
        try {
            if (hasListeners("didReceiveMessage")) {
                KrollDict data = new KrollDict();
                data.put("message", new KrollDict(message));
                fireEvent("didReceiveMessage", data);
            }
        } catch (Exception e) {
            Log.e(LCAT, "Message exception: " + e.getMessage());
        }
    }

    @Kroll.method
    public void createNotificationChannel(KrollDict options) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        Log.d(LCAT, "createNotificationChannel " + options.toString());
        Context context = Utils.getApplicationContext();
        String sound = options.optString("sound", "default");
        String importance = options.optString("importance", sound.equals("silent") ? "low" : "default");
        String channelId = options.optString("channelId", "default");
        String channelName = options.optString("channelName", channelId);
        Boolean vibration = (Boolean) options.optBoolean("vibrate", false);
        Boolean lights = (Boolean) options.optBoolean("lights", false);
        Boolean showBadge = (Boolean) options.optBoolean("showBadge", false);
        int importanceVal = NotificationManager.IMPORTANCE_DEFAULT;
        if (importance.equals("low")) {
            importanceVal = NotificationManager.IMPORTANCE_LOW;
        } else if (importance.equals("high")) {
            importanceVal = NotificationManager.IMPORTANCE_HIGH;
        }

        Uri soundUri = null;
        if (sound.equals("default") || sound.equals("")) {
            soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } else if (!sound.equals("silent")) {
            soundUri = Utils.getSoundUri(sound);
            Log.d(LCAT, "createNotificationChannel with sound " + sound + " at " + soundUri.toString());
        }

        NotificationChannel channel = new NotificationChannel(channelId, channelName, importanceVal);
        channel.enableVibration(vibration);
        channel.enableLights(lights);
        channel.setShowBadge(showBadge);
        if (soundUri != null) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .build();
            channel.setSound(soundUri, audioAttributes);
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

    @Kroll.method
    public void deleteNotificationChannel(String channelId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        Log.d(LCAT, "deleteNotificationChannel " + channelId);

        Context context = Utils.getApplicationContext();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.deleteNotificationChannel(channelId);
    }

    @Kroll.getProperty
    public String fcmToken() {
        if (fcmToken != null) {
            return fcmToken;
        } else {
            getToken();
            return null;
        }
    }

    @Kroll.setProperty
    public void apnsToken(String str) {
        // empty
    }

    // clang-format off
    @Kroll.setProperty
    @Kroll.method
    public void setNotificationChannel(Object channel)
    // clang-format on
    {
        if (!(channel instanceof NotificationChannelProxy)) {
            return;
        }

        Context context = Utils.getApplicationContext();
        NotificationChannelProxy channelProxy = (NotificationChannelProxy) channel;
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channelProxy.getNotificationChannel());
    }

    // clang-format off
    @Kroll.setProperty
    @Kroll.method
    public void setForceShowInForeground(final Boolean showInForeground)
    // clang-format on
    {
        Context context = Utils.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(FORCE_SHOW_IN_FOREGROUND, showInForeground);
        editor.apply();
    }

    @Kroll.getProperty
    public Boolean forceShowInForeground() {
        Context context = Utils.getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(FORCE_SHOW_IN_FOREGROUND, false);
    }

    public void setNotificationData(String data) {
        notificationData = data;
    }

    public void parseBootIntent() {
        try {
            Intent intent = TiApplication.getAppRootOrCurrentActivity().getIntent();
            String notification = intent.getStringExtra("fcm_data");
            if (notification != null) {
                HashMap<String, Object> msg = new HashMap<String, Object>();
                msg.put("data", new KrollDict(new JSONObject(notification)));
                onMessageReceived(msg);
                intent.removeExtra("fcm_data");
            } else {
                Log.d(LCAT, "Empty notification in Intent");
            }
        } catch (Exception ex) {
            Log.e(LCAT, "parseBootIntent" + ex);
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(Utils.getApplicationContext());
        preferences.edit().remove("titanium.firebase.cloudmessaging.message").apply();
    }
}
