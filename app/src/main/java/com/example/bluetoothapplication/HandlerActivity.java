package com.example.bluetoothapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;

public class HandlerActivity extends AppCompatActivity {
    private TextView text_view_counter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_handler);
        text_view_counter = findViewById(R.id.text_view_counter);
        ThreadCounter t = new ThreadCounter();
        t.start();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            text_view_counter.setText(String.valueOf(msg.arg1));
            return false;
        }
    });

    private class ThreadCounter extends Thread {

        public void run()
        {
            for (int i = 0; i < 50; i++) {
                Message message = Message.obtain();
                message.arg1 = i;
                handler.sendMessage(message);
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}