package com.main.chatapp.listeners;

import com.main.chatapp.models.User;

public interface UserListener {
    void onUserClicked(User user);

    void initiateVideoMeeting(User user);
//
    void initiateAudioMeeting(User user);

    void initiateGame(User user);
//
//    void onConversionClicked(User user);
}
