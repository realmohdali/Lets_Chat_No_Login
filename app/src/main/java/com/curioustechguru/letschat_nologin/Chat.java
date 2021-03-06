package com.curioustechguru.letschat_nologin;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.curioustechguru.letschat_nologin.adapter.ChatAdapter;
import com.curioustechguru.letschat_nologin.data.MessageData;
import com.curioustechguru.letschat_nologin.services.BackgroundService;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Chat extends AppCompatActivity {

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
    ActivityResultLauncher<Intent> someActivityResultLauncher;
    String connection = "false";
    ArrayList<MessageData> messageDataArrayList;
    ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
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

        selectImage.setOnClickListener(v -> getImage());

        messageDataArrayList = new ArrayList<>();

        chatView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ChatAdapter(messageDataArrayList);

        chatView.setAdapter(adapter);

        msgBox.setImeActionLabel("SEND", KeyEvent.KEYCODE_ENTER);

        msgBox.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == KeyEvent.KEYCODE_ENTER) {
                sendMessage();
            }
            return true;
        });

        connect();

        checkConnectionServiceIntent = new Intent(this, BackgroundService.class);
        startService(checkConnectionServiceIntent);

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String connectStatus = intent.getStringExtra("data");
                if (connectStatus.equalsIgnoreCase("user connected")) {
                    other_user = intent.getStringExtra("other_user");
                    connection = "true";
                    connected();
                } else {
                    connection = "false";
                    disconnected();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter(BackgroundService.CONNECTION_BROADCAST);
        this.registerReceiver(broadcastReceiver, intentFilter);

        BroadcastReceiver newMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle args = intent.getBundleExtra("msgData");

                ArrayList<MessageData> newMessages = (ArrayList<MessageData>) args.getSerializable("messageData");

                for (int i = 0; i < newMessages.size(); i++) {
                    String msg = newMessages.get(i).getMessage();
                    String type = newMessages.get(i).getType();
                    String sender = newMessages.get(i).getSender();
                    MessageData messageData = new MessageData();
                    messageData.setMessage(msg);
                    messageData.setType(type);
                    messageData.setSender(sender);
                    messageDataArrayList.add(messageData);
                    adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    chatView.scrollToPosition(adapter.getItemCount() - 1);
                }
                //String newMessage = intent.getStringExtra("data");
            }
        };

        IntentFilter newMessageIntentFilter = new IntentFilter(BackgroundService.MESSAGE_BROADCAST);
        this.registerReceiver(newMessageReceiver, newMessageIntentFilter);

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // There are no request codes
                        if (result.getData() != null) {
                            Uri imageUri = result.getData().getData();
                            if (imageUri != null) {
                                try {
                                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                                    final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                                    final BottomSheetDialog dialog = new BottomSheetDialog(this);
                                    View view = getLayoutInflater().inflate(R.layout.send_image, null, false);
                                    dialog.setContentView(view);

                                    ImageView imagePreview = view.findViewById(R.id.image_preview);
                                    ImageView sendImage = view.findViewById(R.id.send_image);
                                    ImageView cancelImageSend = view.findViewById(R.id.cancel_image_send);


                                    imagePreview.setImageBitmap(bitmap);
                                    dialog.setCancelable(false);
                                    dialog.show();
                                    sendImage.setOnClickListener(v -> {
                                        String encodedImage = encodeBitmap(bitmap);
                                        sendImageFile(encodedImage, bitmap);
                                        dialog.dismiss();
                                    });
                                    cancelImageSend.setOnClickListener(v -> dialog.dismiss());

                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                    }
                });

    }

    private String encodeBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();
        return android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void getImage() {

        Dexter.withContext(this).withPermission(Manifest.permission.READ_EXTERNAL_STORAGE).withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                someActivityResultLauncher.launch(Intent.createChooser(intent, "Select Image"));
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                permissionToken.continuePermissionRequest();
            }
        }).check();
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
        if (connection.equalsIgnoreCase("true")) {
            new AlertDialog.Builder(this)
                    .setTitle("Confirm")
                    .setMessage("Are you sure to leave the chat?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        endChat();
                        stopService(checkConnectionServiceIntent);
                        finish();
                    })
                    .setNegativeButton("No", null).show();
        } else {
            finish();
        }
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
                    MessageData messageData = new MessageData();
                    messageData.setMessage(msg);
                    messageData.setType("1");
                    messageData.setSender("me");
                    messageDataArrayList.add(messageData);
                    adapter.notifyItemInserted(adapter.getItemCount() - 1);
                    chatView.scrollToPosition(adapter.getItemCount() - 1);
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

    private void sendImageFile(String encodedImage, Bitmap bitmap) {

        MessageData messageData = new MessageData();
        messageData.setBitmap(bitmap);
        messageData.setType("2");
        messageData.setSender("me");
        messageDataArrayList.add(messageData);
        adapter.notifyItemInserted(adapter.getItemCount() - 1);
        chatView.scrollToPosition(adapter.getItemCount() - 1);

        int index = adapter.getItemCount() - 1;

        String url = "https://chat.curioustechguru.com/upload_image_android.php";
        //Toast.makeText(this, "Send image is called", Toast.LENGTH_SHORT).show();

        // Request a string response from the provided URL.
        stringRequest = new StringRequest(Request.Method.POST,
                url,
                response -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.getString("code").equalsIgnoreCase("200")) {
                            String imageUrl = jsonObject.getString("img_url");
                            MessageData newMessageData = new MessageData();
                            newMessageData.setMessage(imageUrl);
                            newMessageData.setType("3");
                            newMessageData.setSender("me");
                            messageDataArrayList.set(index, newMessageData);
                            adapter.notifyItemChanged(index);
                        } else {
                            Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, error -> {
        }) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("other_user", other_user);
                params.put("image", encodedImage);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        stringRequest.setTag(TAG);
    }
}