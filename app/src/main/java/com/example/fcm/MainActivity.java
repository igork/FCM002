package com.example.fcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import static com.example.fcm.R.id.txt;




public class MainActivity extends AppCompatActivity {
	private static final String AUTH_KEY = "key=AIzaSyCarJhFL2md6npvDKq_uDNWwNCaqHX9qDo";
	private TextView mTextView;
	private String token;
	private String topic = "igors_channel"; //"news";
	private String TAG = "FCM002.";


	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			/*
			if (intent.getExtras()!=null){
				String from = intent.getExtras().getString("from");
				String dataBody = intent.getExtras().getString("dataBody");
				String dataTitle = intent.getExtras().getString("dataTitle");
				String notification = intent.getExtras().getString("notification");

				String msg = null;
				MyMessageOne mm = parseMyMessage(dataTitle,dataBody);
				if (mm!=null){

					dealWithMyMessageOne(mm);

				} else {

					msg =
							"from: " + from + "\n" +
									"data: title: " + dataTitle + "\n" +
									"data: body: " + dataBody + "\n" +
									"notification: " + notification;
					Log.d(INTENT_TAG,msg);
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
				}
			}

			 */
			Bundle bundle = getIntent().getExtras();
			if (bundle!=null) {
				String tmp = "";
				for (String key : bundle.keySet()) {
					Object value = bundle.get(key);
					tmp += "BroadcastReceiver: " + key + ": " + value + "\n";
				}
				mTextView.setText(tmp);
				Log.d(TAG, tmp);
			} else {
				String tmp = "empty bundle";
				mTextView.setText(tmp);
				Log.d(TAG, tmp);
			}

		}
	};

	@Override
	protected void onStop() {
		super.onStop();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver)
				,
				new IntentFilter("MyData")
		);

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mTextView = findViewById(txt);
		//make scrollable + android:orientation="vertical" to manifest
		mTextView.setMovementMethod(new ScrollingMovementMethod());

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {

			/*
	for notification when device is inactive

    google.sent_time: 1572112387003
    google.ttl: 2419200
    google.original_priority: high
    picture: http://igorkourski.000webhostapp.com/gallery_gen/2cdc5a7b50c25c418057f0753ee99da4_372x372.jpg
    body: Data Body
    from: 878978240542
    text: Data Text
    title: Data Title
    google.message_id: 0:1572112387024943%2adabfdf2adabfdf
    collapse_key: com.example.fcm
			 */


			String tmp = "";
			for (String key : bundle.keySet()) {
				Object value = bundle.get(key);
				tmp += "getIntent: " + key + ": " + value + "\n";
			}
			mTextView.setText(tmp);
			Log.d(TAG,tmp);
		}

		FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
			@Override
			public void onComplete(@NonNull Task<InstanceIdResult> task) {
				if (!task.isSuccessful()) {
					token = task.getException().getMessage();
					Log.w(TAG + "FCM TOKEN Failed", task.getException());
				} else {
					token = task.getResult().getToken();
					Log.d(TAG + "FCM TOKEN", token);
				}
			}
		});
	}

	public void showToken(View view) {
		mTextView.setText(token);
		Log.i(TAG + "FCM TOKEN", token);
	}

	public void subscribe(View view) {
		FirebaseMessaging.getInstance().subscribeToTopic(topic);
		String text = "Subscribed to " + topic + " topic";
		mTextView.setText(text);
		Log.d(TAG,text);
	}

	public void unsubscribe(View view) {
		FirebaseMessaging.getInstance().unsubscribeFromTopic("news");
		String text = "Unsubscribed from " + topic + " topic";
		mTextView.setText(text);
		Log.d(TAG,text);
	}

	public void sendToken(View view) {
		sendWithOtherThread("token");
	}

	public void sendTokens(View view) {
		sendWithOtherThread("tokens");
	}

	public void sendTopic(View view) {
		sendWithOtherThread("topic");
	}

	private void sendWithOtherThread(final String type) {
		Log.d(TAG,"Action: " + type);
		new Thread(new Runnable() {
			@Override
			public void run() {
				pushNotification(type);
			}
		}).start();
	}

	private void pushNotification(String type) {
		JSONObject jPayload = new JSONObject();
		JSONObject jNotification = new JSONObject();
		JSONObject jData = new JSONObject();
		try {
			jNotification.put("title", "Google I/O 2016");
			jNotification.put("body", "Firebase Cloud Messaging (App) 002");
			jNotification.put("sound", "default");
			jNotification.put("badge", "1");
			jNotification.put("click_action", "OPEN_ACTIVITY_1");
			jNotification.put("icon", "ic_notification");

			jData.put("picture", "https://miro.medium.com/max/1400/1*QyVPcBbT_jENl8TGblk52w.png");

			jData.put("title","placeholder");
			jData.put("body",type);

			switch(type) {
				case "tokens":
					JSONArray ja = new JSONArray();
					ja.put("c5pBXXsuCN0:APA91bH8nLMt084KpzMrmSWRS2SnKZudyNjtFVxLRG7VFEFk_RgOm-Q5EQr_oOcLbVcCjFH6vIXIyWhST1jdhR8WMatujccY5uy1TE0hkppW_TSnSBiUsH_tRReutEgsmIMmq8fexTmL");
					ja.put(token);
					jPayload.put("registration_ids", ja);
					break;
				case "topic":
					jPayload.put("to", "/topics/" + topic);
					break;
				case "condition":
					jPayload.put("condition", "'sport' in topics || 'news' in topics");
					break;
				default:
					jPayload.put("to", token);
			}

			jPayload.put("priority", "high");
			jPayload.put("notification", jNotification);
			jPayload.put("data", jData);

			Log.d(TAG,"payload: " + jPayload.toString());

			URL url = new URL("https://fcm.googleapis.com/fcm/send");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Authorization", AUTH_KEY);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			// Send FCM message content.
			OutputStream outputStream = conn.getOutputStream();
			outputStream.write(jPayload.toString().getBytes());

			// Read FCM response.
			InputStream inputStream = conn.getInputStream();
			final String resp = convertStreamToString(inputStream);

			Log.d(TAG,"response: " + resp);

			/*
			Handler h = new Handler(Looper.getMainLooper());
			h.post(new Runnable() {
				@Override
				public void run() {
					mTextView.setText(resp);
				}
			});
			*/
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}

	private String convertStreamToString(InputStream is) {
		Scanner s = new Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next().replace(",", ",\n") : "";
	}
}