package my.edu.utar.evercare.RemoteMonitoring;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig;
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment;

import my.edu.utar.evercare.R;

public class LiveActivity extends AppCompatActivity {

    String userID, name, liveId;
    boolean isHost;
    TextView liveIdText;
    ImageView shareBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live);

        liveIdText= findViewById(R.id.live_id_input);
        shareBtn= findViewById(R.id.share_btn);

        userID= getIntent().getStringExtra("user_id");
        name= getIntent().getStringExtra("name");
        liveId= getIntent().getStringExtra("live_id");
        isHost= getIntent().getBooleanExtra("host",false);

        liveIdText.setText((liveId));

        addFragment();

        shareBtn.setOnClickListener((v)->{
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,"Join my live in EverCare App \n Live ID: " + liveId);
            startActivity(Intent.createChooser(intent, "Share via"));
        });
    }

    void addFragment(){
        ZegoUIKitPrebuiltLiveStreamingConfig config;
        if(isHost){
            config = ZegoUIKitPrebuiltLiveStreamingConfig.host();
        }else{
            config = new ZegoUIKitPrebuiltLiveStreamingConfig().audience();
        }

        ZegoUIKitPrebuiltLiveStreamingFragment fragment = new ZegoUIKitPrebuiltLiveStreamingFragment().newInstance(
                AppConstants.appId,AppConstants.appSign,userID,name,liveId,config);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commitNow();
    }
}