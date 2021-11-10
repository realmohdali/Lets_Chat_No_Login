package com.example.letschat_nologin.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class checkConnection extends Service {
    public static final String CONNECTION_BROADCAST = "connection";
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
        startCheckConnection();
        return START_STICKY;
    }

    private void startCheckConnection() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
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
                //stringRequest.setTag(TAG);
            }
        }, 0, 2000);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
