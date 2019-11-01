package com.example.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

	private static final String CHANNEL_NAME = "FCM";
	private static final String CHANNEL_DESC = "Firebase Cloud Messaging";
	private int numMessages = 0;
	private String TAG = "FCM002.Service.";

	public static final String FCM_PARAM = "picture";

	public static final String FCM_TITLE = "title";
	public static final String FCM_BODY = "body";

	private LocalBroadcastManager broadcaster;

	public MyFirebaseMessagingService(){
		broadcaster = LocalBroadcastManager.getInstance(this);;
	}

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		super.onMessageReceived(remoteMessage);
		RemoteMessage.Notification notification = remoteMessage.getNotification();
		Map<String, String> data = remoteMessage.getData();

		Log.d(TAG +"From", remoteMessage.getFrom());
		Log.d(TAG +"Data", data.toString());
		Log.d(TAG +"Notification", notification.toString());

		sendNotification(notification, data);
		//sendBbroadcast(notification, data);
	}

	/*
	https://medium.com/@ankit_aggarwal/ways-to-communicate-between-activity-and-service-6a8f07275297
	https://blog.mindorks.com/using-localbroadcastmanager-in-android
	*/
	private void sendBbroadcast(RemoteMessage.Notification notification, Map<String, String> data) {

		/*
		Intent intent = new Intent("MyData");

		Log.d(TAG, "from: " + remoteMessage.getFrom());
		intent.putExtra("from",remoteMessage.getFrom());

		// Check if message contains a data payload.
		if (data!=null && !data.isEmpty()) {

			String dataBody = data.get("body");
			intent.putExtra("dataBody", dataBody);

			String dataTitle = data.get("title");
			intent.putExtra("dataTitle", dataTitle);
		}

		// Check if message contains a notification payload.
		if (remoteMessage.getNotification() != null) {
			Map<String,String> map = new HashMap<String,String>();
			map.put("body",remoteMessage.getNotification().getBody());
			map.put("title",remoteMessage.getNotification().getTitle());
			Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
			Log.d(TAG, "Message Notification Title: " + remoteMessage.getNotification().getTitle());

			intent.putExtra("notification",map.toString());
			//Toast.makeText(this, "notification: " + remoteMessage.getNotification().getBody(), Toast.LENGTH_SHORT).show();
		}

		broadcaster.sendBroadcast(intent);
		*/
		/*
		Bundle bundle = new Bundle();

		bundle.putString(FCM_PARAM, data.get(FCM_PARAM));
		bundle.putString(FCM_TITLE, data.get(FCM_TITLE));
		bundle.putString(FCM_BODY, data.get(FCM_BODY));

		Intent intent = new Intent("MyData");//new Intent(this, MainActivity.class,); //Intent intent = new Intent(this, SecondActivity.class);
		intent.putExtras(bundle);
		*/
		Intent intent = new Intent("MyData");
		if (data!=null && !data.isEmpty()) {

			String dataBody = data.get("body");
			intent.putExtra("dataBody", dataBody);

			String dataTitle = data.get("title");
			intent.putExtra("dataTitle", dataTitle);
		}

		broadcaster.sendBroadcast(intent);

	}

	private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data) {
		Bundle bundle = new Bundle();

		bundle.putString(FCM_PARAM, data.get(FCM_PARAM));
		bundle.putString(FCM_TITLE, data.get(FCM_TITLE));
		bundle.putString(FCM_BODY, data.get(FCM_BODY));

		Intent intent = new Intent(this, MainActivity.class); //Intent intent = new Intent(this, SecondActivity.class);
		intent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, getString(R.string.notification_channel_id))
				.setContentTitle(notification.getTitle())
				.setContentText(notification.getBody())

				.setAutoCancel(true)
				.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
				//.setSound(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.win))
				.setContentIntent(pendingIntent)
				.setContentInfo("Hello")
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
				//.setColor(getColor(R.color.colorAccent)) //require sdk 23
				.setLights(Color.RED, 1000, 300)
				.setDefaults(Notification.DEFAULT_VIBRATE)
				.setNumber(++numMessages)
				.setSmallIcon(R.drawable.ic_notification);

		try {
			String picture = data.get(FCM_PARAM);
			if (picture != null && !"".equals(picture)) {
				URL url = new URL(picture);
				Bitmap bigPicture = BitmapFactory.decodeStream(url.openConnection().getInputStream());
				notificationBuilder.setStyle(
						new NotificationCompat.BigPictureStyle().bigPicture(bigPicture).setSummaryText(notification.getBody())
				);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
					getString(R.string.notification_channel_id), CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
			);
			channel.setDescription(CHANNEL_DESC);
			channel.setShowBadge(true);
			channel.canShowBadge();
			channel.enableLights(true);
			channel.setLightColor(Color.RED);
			channel.enableVibration(true);
			channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

			assert notificationManager != null;
			notificationManager.createNotificationChannel(channel);
		}

		assert notificationManager != null;
		notificationManager.notify(0, notificationBuilder.build());
	}
}