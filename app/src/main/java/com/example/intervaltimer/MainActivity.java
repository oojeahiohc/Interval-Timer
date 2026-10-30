package com.example.intervaltimer;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
// XML 화면을 연결하고 버튼 클릭을 처리하는 파일
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "IntervalTimer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView titleText = findViewById(R.id.titleText);
        Button changeButton = findViewById(R.id.changeButton);

        Log.d(TAG, "MainActivity created");

        changeButton.setOnClickListener(view -> {
            titleText.setText(R.string.message_changed);
            Log.d(TAG, "Title changed by button click");
        });
    }
}