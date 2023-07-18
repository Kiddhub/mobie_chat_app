package com.main.chatapp.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.main.chatapp.R;

public class WinDialog extends Dialog {
    private final String message;
    private final TicTacToeActivity ticTacToeActivity;
    public WinDialog(@NonNull Context context,String message) {
        super(context);
        this.message = message;
        this.ticTacToeActivity = ((TicTacToeActivity) context);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.win_dialog_layout);


        final TextView messageTV = findViewById(R.id.messageTV);
        final Button startBtn = findViewById(R.id.startNewBtn);

        messageTV.setText(message);

        startBtn.setOnClickListener(view -> {
            dismiss();
            getContext().startActivity(new Intent(getContext(), PlayerName.class));
            ticTacToeActivity.finish();
        });
    }
}
