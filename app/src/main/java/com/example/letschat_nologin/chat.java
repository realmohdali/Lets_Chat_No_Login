package com.example.letschat_nologin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.letschat_nologin.services.checkConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class chat extends AppCompatActivity {

    private static final String TAG = "MyTag";
    ProgressBar progressBar;
    TextView noOne;
    Button tryAgain;
    ImageView endChatBtn, selectImage, sendBtn;
    LinearLayout chatMsgArea;
    RecyclerView chatView;
    StringRequest stringRequest;
    RequestQueue queue;
    int myInterestInt, myGenderInt;
    String other_user;
    EditText msgBox;
    Intent checkConnectionServiceIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        progressBar = findViewById(R.id.progress_bar);
        noOne = findViewById(R.id.no_one);
        tryAgain = findViewById(R.id.try_again);
        chatMsgArea = findViewById(R.id.chat_msg_area);
        chatView = findViewById(R.id.chat_view);
        endChatBtn = findViewById(R.id.end_chat_btn);
        selectImage = findViewById(R.id.select_image);
        sendBtn = findViewById(R.id.send_btn);
        msgBox = findViewById(R.id.msg_box);

        Intent intent = getIntent();
        String myInterest = intent.getStringExtra("myInterest");
        String myGender = intent.getStringExtra("myGender");

        myInterestInt = stringToInt(myInterest);
        myGenderInt = stringToInt(myGender);

        queue = Volley.newRequestQueue(this);

        endChatBtn.setOnClickListener(v -> disconnect());

        tryAgain.setOnClickListener(v -> connect());

        sendBtn.setOnClickListener(v -> sendMessage());

        connect();

        checkConnectionServiceIntent = new Intent(this, checkConnection.class);
        startService(checkConnectionServiceIntent);

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String connectStatus = intent.getStringExtra("data");
                if (connectStatus.equalsIgnoreCase("user connected")) {
                    other_user = intent.getStringExtra("other_user");
                    connected();
                } else {
                    disconnected();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(checkConnection.CONNECTION_BROADCAST);
        this.registerReceiver(broadcastReceiver, intentFilter);

    }


    private int stringToInt(String x) {
        int ret = 0;
        if (x.equalsIgnoreCase("Male")) {
            ret = 1;
        } else if (x.equalsIgnoreCase("Female")) {
            ret = 2;
        } else if (x.equalsIgnoreCase("Both")) {
            ret = 3;
        }
        return ret;
    }


    @Override
    public void onBackPressed() {
        disconnect();
    }

    private void disconnect() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure to leave the chat?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    endChat();
                    stopService(checkConnectionServiceIntent);
                    finish();
                })
                .setNegativeButton("No", null).show();
    }

    private void connect() {
        noOne.setVisibility(View.GONE);
        tryAgain.setVisibility(View.GONE);
        chatMsgArea.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        String url = "https://chat.curioustechguru.com/script/connect_user.php";

        // Request a string response from the provided URL.
        stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                    try {
                        /*JSONArray jsonArray = new JSONArray(response);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);

                            String code = jsonObject.getString("code");
                            other_user = jsonObject.getString("other_user");*/

                        JSONObject jsonObject = new JSONObject(response);
                        String code = jsonObject.getString("code");
                        other_user = jsonObject.getString("other_user");

                        progressBar.setVisibility(View.GONE);
                        if (code.equals("200")) {
                            chatMsgArea.setVisibility(View.VISIBLE);
                        } else {
                            noOne.setVisibility(View.VISIBLE);
                            tryAgain.setVisibility(View.VISIBLE);
                        }
                        //}
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {

        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                String gen = myGenderInt + "";
                String inte = myInterestInt + "";
                params.put("gender", gen);
                params.put("interest", inte);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        stringRequest.setTag(TAG);
    }

    private void connected() {
        progressBar.setVisibility(View.GONE);
        noOne.setVisibility(View.GONE);
        tryAgain.setVisibility(View.GONE);
        chatMsgArea.setVisibility(View.VISIBLE);
    }

    private void disconnected() {
        chatMsgArea.setVisibility(View.GONE);
        Toast.makeText(this, "Other user left", Toast.LENGTH_SHORT).show();
    }

    private void endChat() {
        String url = "https://chat.curioustechguru.com/script/end_chat.php";

        // Request a string response from the provided URL.
        stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                }, error -> {

        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("other_user", other_user);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        stringRequest.setTag(TAG);
    }

    private void sendMessage() {
        String msg = msgBox.getText().toString();
        msgBox.setText("");
        String url = "https://chat.curioustechguru.com/script/send_message.php";

        // Request a string response from the provided URL.
        stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                }, error -> {
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("other_user", other_user);
                params.put("message", msg);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        stringRequest.setTag(TAG);
    }

    private void sendImage() {

    }

    private void getMessage() {

    }

}