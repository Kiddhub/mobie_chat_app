package com.main.chatapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.installations.FirebaseInstallations;
import com.main.chatapp.R;
import com.main.chatapp.databinding.ActivityOutgoingInvitationBinding;
import com.main.chatapp.models.User;
import com.main.chatapp.network.ApiClient;
import com.main.chatapp.network.ApiService;
import com.main.chatapp.utilities.Constants;
import com.main.chatapp.utilities.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    private ActivityOutgoingInvitationBinding binding;
    private PreferenceManager preferenceManager;
    private String inviterToken = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());
        String meetingType = getIntent().getStringExtra("type");
        if (meetingType != null && meetingType.equals("video")) {
            binding.imageMeetingType.setImageResource(R.drawable.ic_video);
        }

        User user = (User) getIntent().getSerializableExtra("user");
        if (user != null) {
            binding.textName.setText(user.name);
        }

        binding.imageStopInvitation.setOnClickListener(v -> {
            if (user != null){
                cancelInvitation(user.token);
            }

        });
        FirebaseInstallations.getInstance().getToken(true).addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                inviterToken = task.getResult().getToken();
                if (meetingType != null && user != null) {
                    initiateMeeting(meetingType, user.token);
                }
            }
        });


    }

    private void initiateMeeting(String meetingType, String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();
            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
            data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, inviterToken);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void cancelInvitation(String receiverToken) {
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);
            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);
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
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            if (type.equals(Constants.REMOTE_MSG_INVITATION)) {
                                Toast.makeText(OutgoingInvitationActivity.this, "Invitation sent", Toast.LENGTH_SHORT).show();
                            } else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)) {
                                Toast.makeText(OutgoingInvitationActivity.this, "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(OutgoingInvitationActivity.this, response.message(), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        Toast.makeText(OutgoingInvitationActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null) {
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)) {
                    Toast.makeText(context, "Invitation Accepted", Toast.LENGTH_SHORT).show();
                } else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)) {
                    Toast.makeText(context, "Invitation Rejected", Toast.LENGTH_SHORT).show();
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