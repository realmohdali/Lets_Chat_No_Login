package com.curioustechguru.letschat_nologin;


import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyTag";
    Button btn;
    RadioGroup gender, interest;
    CardView cardView;
    ProgressBar progressBar;
    StringRequest stringRequest;
    RequestQueue queue;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);
        if (!initUser().get()) {
            Toast.makeText(this, "Error in initializing user", Toast.LENGTH_SHORT).show();
            System.exit(0);
        }
        btn = findViewById(R.id.start_chat_btn);
        gender = (RadioGroup) findViewById(R.id.gender_radio);
        interest = (RadioGroup) findViewById(R.id.interest_radio);
        cardView = findViewById(R.id.cardView);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.text_view);

        TextView privacyLink = findViewById(R.id.privacy_link);
        privacyLink.setMovementMethod(LinkMovementMethod.getInstance());

        btn.setOnClickListener(v -> {
            int selectedGender = gender.getCheckedRadioButtonId();
            int selectedInterest = interest.getCheckedRadioButtonId();
            RadioButton genderButton = findViewById(selectedGender);
            RadioButton interestButton = findViewById(selectedInterest);

            String myGender = genderButton.getText().toString();
            String myInterest = interestButton.getText().toString();

            CheckBox checkBox = findViewById(R.id.checkbox);
            if(checkBox.isChecked()) {
                startChat(myGender, myInterest);
            } else {
                Toast.makeText(this, "Please check the declaration", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!initUser().get()) {
            Toast.makeText(this, "Error in initializing user", Toast.LENGTH_SHORT).show();
            System.exit(0);
        }
        cardView.setVisibility(View.VISIBLE);
    }

    protected AtomicBoolean initUser() {
        AtomicBoolean success = new AtomicBoolean(true);
        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);
        String url = "https://chat.curioustechguru.com/script/init_user.php";

        // Request a string response from the provided URL.
        stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            // Display the first 500 characters of the response string.
            progressBar.setVisibility(View.GONE);
            success.set(response.equals("200"));

        }, error -> success.set(false));

        // Add the request to the RequestQueue.
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        stringRequest.setTag(TAG);
        return success;
    }

    protected void startChat(String myGender, String myInterest) {
        cardView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        queue = Volley.newRequestQueue(this);
        String url = "https://chat.curioustechguru.com/script/add_user.php";

        // Request a string response from the provided URL.
        stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            progressBar.setVisibility(View.GONE);
            if(response.equals("200")) {
                Intent intent = new Intent(MainActivity.this, Chat.class);
                intent.putExtra("myGender", myGender);
                intent.putExtra("myInterest", myInterest);
                startActivity(intent);
            } else {
                cardView.setVisibility(View.VISIBLE);
                textView.setText(response);
            }
        }, error -> cardView.setVisibility(View.VISIBLE)){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("gender", myGender);
                params.put("interest", myInterest);
                return params;
            }
        };

        // Add the request to the RequestQueue.
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
        stringRequest.setTag(TAG);
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }
}