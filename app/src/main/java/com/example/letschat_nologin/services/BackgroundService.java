package com.example.letschat_nologin.services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.letschat_nologin.data.MessageData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class BackgroundService extends Service {
    public static final String CONNECTION_BROADCAST = "connection";
    public static final String MESSAGE_BROADCAST = "message";
    private String connection = "false";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkConnection();
                getMessage();
            }
        }, 0, 3000);
        checkConnection();
        return START_STICKY;
    }

    private void getMessage() {
        String url = "https://chat.curioustechguru.com/script/get_message.php";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                url,
                response -> {
                    if (!response.isEmpty()) {
                        try {
                            ArrayList<MessageData> messageDataArrayList = new ArrayList<>();
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String type = jsonObject.getString("type");
                                String msg = jsonObject.getString("message");
                                MessageData messageData = new MessageData();
                                messageData.setType(type);
                                messageData.setMessage(msg);
                                messageData.setSender("other");
                                messageDataArrayList.add(messageData);
                            }
                            Bundle args = new Bundle();
                            args.putSerializable("messageData",(Serializable) messageDataArrayList);
                            Intent intent = new Intent();
                            intent.setAction(MESSAGE_BROADCAST);
                            intent.putExtra("data", "New Message Received");
                            intent.putExtra("msgData", args);
                            sendBroadcast(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, error -> {

        });

        // Add the request to the RequestQueue.
        stringRequest.setShouldCache(false);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    private void checkConnection() {
        String url = "https://chat.curioustechguru.com/script/check_connection.php";

        //Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String code = jsonObject.getString("code");

                            if (code.equals("200")) {
                                if (connection.equals("false")) {
                                    connection = jsonObject.getString("connection");
                                    Intent intent = new Intent();
                                    intent.setAction(CONNECTION_BROADCAST);
                                    intent.putExtra("data", "user connected");
                                    String other_user = jsonObject.getString("other_user");
                                    intent.putExtra("other_user", other_user);
                                    sendBroadcast(intent);
                                }
                            } else {
                                if (connection.equals("true")) {
                                    connection = jsonObject.getString("connection");
                                    Intent intent = new Intent();
                                    intent.setAction(CONNECTION_BROADCAST);
                                    intent.putExtra("data", "user disconnected");
                                    sendBroadcast(intent);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //msgBox.setText("");
                params.put("connection", "false");
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setShouldCache(false);
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
        //stringRequest.setTag(TAG)

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
