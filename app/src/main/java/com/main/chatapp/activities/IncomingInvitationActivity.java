package com.main.chatapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.main.chatapp.R;
import com.main.chatapp.databinding.ActivityIncomingInvitationBinding;
import com.main.chatapp.network.ApiClient;
import com.main.chatapp.network.ApiService;
import com.main.chatapp.utilities.Constants;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {

    private String meetingType = null;

    private ActivityIncomingInvitationBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ImageView imageMeetingType = findViewById(R.id.imageMeetingType);
        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);

        if (meetingType != null){
            if(meetingType.equals("video")){
                binding.imageMeetingType.setImageResource(R.drawable.ic_video);
            }else if(meetingType.equals("audio")) {
                binding.imageMeetingType.setImageResource(R.drawable.ic_audio);
            }else{
                binding.imageMeetingType.setImageResource(R.drawable.ic_game);
            }
        }
        String name = getIntent().getStringExtra(Constants.KEY_NAME);
        if (name != null) {
            binding.textName.setText(name);
        }

        binding.imageAcceptInvitation.setOnClickListener(view ->
                sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));
        binding.imageRejectInvitation.setOnClickListener(view -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED
                , getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

    }

    private void sendInvitationResponse(String type, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendMessage(body.toString(), type);
            finish();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void sendMessage(String remoteMessageBody, String type) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(), remoteMessageBody)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull  Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                                if(meetingType.equals("audio") || meetingType.equals("video")){
                                    try{
                                        URL serverURL = new URL("https://meet.jit.si");
                                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                                        builder.setServerURL(serverURL);
                                        builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));

                                        if(meetingType.equals("audio")){
                                            builder.setVideoMuted(true);
                                        }
                                        JitsiMeetActivity.launch(IncomingInvitationActivity.this,builder.build());
                                        finish();
                                    }catch (Exception e){
                                        Toast.makeText(IncomingInvitationActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } else if (meetingType.equals("game")) {
                                    Intent intent = new Intent(getApplicationContext(), PlayerName.class);
                                    startActivity(intent);
                                }

                            } else{
                                Toast.makeText(IncomingInvitationActivity.this, "Invitation Rejected", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(IncomingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    }
                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        Toast.makeText(IncomingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }
    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if(type != null){
                if(type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)){
                    Toast.makeText(context,"Invitation Cancelled", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}