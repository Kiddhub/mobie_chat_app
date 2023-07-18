package com.main.chatapp.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import com.main.chatapp.R;

public class PlayerName extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_name);

        final EditText playerNameEt = findViewById(R.id.playerNameText);
        final AppCompatButton startGameBtn = findViewById(R.id.startGameButton);
        startGameBtn.setOnClickListener(view -> {
            final String getPlayerName = playerNameEt.getText().toString();

            if(getPlayerName.isEmpty()){
                Toast.makeText(PlayerName.this,"Enter the name",Toast.LENGTH_SHORT).show();
            }
            else {
                //creating inten to open Main
                Intent intent = new Intent(PlayerName.this, TicTacToeActivity.class);

                //adding player name along with intent
                intent.putExtra("playerName",getPlayerName);

                startActivity(intent);

                finish();
            }
        });

    }
}