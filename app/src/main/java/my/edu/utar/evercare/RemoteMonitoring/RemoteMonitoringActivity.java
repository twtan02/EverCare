package my.edu.utar.evercare.RemoteMonitoring;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.Random;
import java.util.UUID;

import my.edu.utar.evercare.R;

public class RemoteMonitoringActivity extends AppCompatActivity {

    Button goLiveBtn;
    TextInputEditText liveIdInput, nameInput;
    String liveId, name, userID;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_monitoring);

        sharedPreferences = getSharedPreferences("name_pref",MODE_PRIVATE);

        goLiveBtn = findViewById(R.id.go_live_btn);
        liveIdInput = findViewById(R.id.live_id_input);
        nameInput = findViewById(R.id.name_input);

        nameInput.setText(sharedPreferences.getString("name",""));

        liveIdInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                liveId = liveIdInput.getText().toString();
                if(liveId.length()==0){
                    goLiveBtn.setText("Start new Live");
                }else{
                    goLiveBtn.setText("Join a Live");
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        goLiveBtn.setOnClickListener((v)->{
            name= nameInput.getText().toString();
            if (name.isEmpty()){
                nameInput.setError("Name is required");
                nameInput.requestFocus();
                return;
            }

            liveId = liveIdInput.getText().toString();
            if(liveId.length()>0 && liveId.length()!=5){
                liveIdInput.setError("Invalid Live ID");
                liveIdInput.requestFocus();
                return;
            }
            startMeeting();


        });

    }


    void startMeeting(){
        sharedPreferences.edit().putString("name",name).apply();
        Log.i("Log", "Start Meeting");

        boolean isHost = true;
        if (liveId.length()==5)
            isHost= false;
        else
            liveId= generateLiveID();

        userID= UUID.randomUUID().toString();

        Intent intent = new Intent(RemoteMonitoringActivity.this, LiveActivity.class);
        intent.putExtra("user_id", userID);
        intent.putExtra("name", name);
        intent.putExtra("live_id", liveId);
        intent.putExtra("host", isHost);
        startActivity(intent);
    }


    String generateLiveID(){
        StringBuilder id = new StringBuilder();
        while (id.length()!=5){
            int random = new Random().nextInt(10);
            id.append(random);
        }
        return id.toString();
    }
}
