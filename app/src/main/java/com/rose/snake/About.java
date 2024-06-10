package com.rose.snake;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class About extends AppCompatActivity {
    Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.about_main);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setOnClickListener(view -> finish());
    }
}
