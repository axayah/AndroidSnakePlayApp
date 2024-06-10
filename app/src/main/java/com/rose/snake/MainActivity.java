package com.rose.snake;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.content.Intent;

public class MainActivity extends AppCompatActivity {
    // Объявляем кнопки для начала игры и перехода к экрану "Об игре"
    Button playBtn;
    Button aboutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Устанавливаем содержимое Activity из XML-разметки
        setContentView(R.layout.activity_main);

        // Связываемся с кнопкой "Играть" и задаем текст кнопке
        playBtn = findViewById(R.id.playButton);
        playBtn.setText("Играть");

        // Устанавливаем слушатель на кнопку "Играть", который будет перенаправлять пользователя к игре
        playBtn.setOnClickListener(view -> {
            // Создаем намерение (Intent) для запуска SnakeActivity, где находится игра "Змейка"
            Intent intent = new Intent(MainActivity.this, SnakeActivity.class);
            // Запускаем SnakeActivity
            startActivity(intent);
        });

        // Связываемся с кнопкой "Об игре" и задаем текст
        aboutBtn = findViewById(R.id.aboutButton);
        aboutBtn.setText("Об игре");

        // Устанавливаем слушатель на кнопку "Об игре", который будет перенаправлять пользователя к экрану информации
        aboutBtn.setOnClickListener(view -> {
            // Создаем намерение (Intent) для запуска экрана About, где представлена информация об игре
            Intent intent = new Intent(MainActivity.this, About.class);
            // Запускаем экран About
            startActivity(intent);
        });
    }
}