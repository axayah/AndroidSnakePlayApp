package com.rose.snake;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.content.Intent;

import java.util.concurrent.atomic.AtomicInteger;

public class SnakeActivity extends AppCompatActivity {
    // Устанавливаем количество кадров в секунду - частота обновления игрового экрана
    private static final int FPS = 60;
    // Устанавливаем скорость змейки в игровых единицах
    private static final int SPEED = 25;
    // Определяем константы для статусов игры
    private static final int STATUS_PAUSED = 1; // Игра на паузе
    private static final int STATUS_START = 2; // Стартовый статус игры
    private static final int STATUS_OVER = 3; // Игра окончена
    private static final int STATUS_PLAYING = 4; // Игра активна
    // Объявляем переменные для взаимодействия с пользовательским интерфейсом
    private GameView mGameView;          // Custom view для игровой области
    private TextView mGameStatusText;    // Текстовое поле для статуса игры
    private TextView mGameScoreText;     // Текстовое поле для отображения очков
    private Button mGameBtn;             // Кнопка управления игрой (старт/пауза)
    private Button mGameExitBtn; // Кнопка для выхода из игры
    // Атомарная переменная для отслеживания и управления текущим статусом игры (для thread-safe операций)
    private final AtomicInteger mGameStatus = new AtomicInteger(STATUS_START);
    // Обработчик для выполнения задач в основном потоке пользовательского интерфейса
    private final Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Устанавливаем содержимое Activity из layout-ресурса snake_main.xml
        setContentView(R.layout.snake_main);

        // Связываем пользовательский интерфейс с кодом через идентификаторы ресурсов
        mGameExitBtn = findViewById(R.id.exit_btn);         // Кнопка выхода
        mGameView = findViewById(R.id.game_view);           // Игровое поле
        mGameStatusText = findViewById(R.id.game_status);   // Текст статуса игры
        mGameBtn = findViewById(R.id.game_control_btn);     // Кнопка управления (старт/пауза)
        mGameScoreText = findViewById(R.id.game_score);     // Текст счета
        // Инициализируем игровое поле
        mGameView.init();
        // Устанавливаем слушателя для обновления игрового счета
        mGameView.setGameScoreUpdatedListener(score -> {
            // Обновляем текст счета в основном потоке приложения
            mHandler.post(() -> mGameScoreText.setText("СЧЕТ: " + score));
        });
        // Устанавливаем слушателей нажатия на кнопки управления
        findViewById(R.id.up_btn).setOnClickListener(v -> {
            // При игре устанавливаем направление движения змейки на 'Вверх'
            if (mGameStatus.get() == STATUS_PLAYING) {
                mGameView.setDirection(Direction.UP);
            }
        });
        // Аналогично для других направлений...
        findViewById(R.id.down_btn).setOnClickListener(v -> { //для 'Вниз'
            if (mGameStatus.get() == STATUS_PLAYING) {
                mGameView.setDirection(Direction.DOWN);
            }
        });
        findViewById(R.id.left_btn).setOnClickListener(v -> { //для 'Влево'
            if (mGameStatus.get() == STATUS_PLAYING) {
                mGameView.setDirection(Direction.LEFT);
            }
        });
        findViewById(R.id.right_btn).setOnClickListener(v -> { //для 'Вправо'
            if (mGameStatus.get() == STATUS_PLAYING) {
                mGameView.setDirection(Direction.RIGHT);
            }
        });
        // Устанавливаем слушатель для кнопки выхода
        findViewById(R.id.exit_btn).setOnClickListener(v -> {
            // Создаем намерение для запуска MainActivity
            Intent intent = new Intent(SnakeActivity.this, MainActivity.class);
            // Очистка истории активностей, чтобы пользователь не мог вернуться назад с MainActivity с помощью кнопки "Назад"
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Запускаем MainActivity и завершаем текущую Activity
            startActivity(intent);
            finish(); // Завершаем SnakeActivity
        });

        // Устанавливаем слушатель для кнопки управления игрой
        mGameBtn.setOnClickListener(v -> {
            // Переключаем статус игры между 'Играем' и 'Пауза'
            if (mGameStatus.get() == STATUS_PLAYING) {
                setGameStatus(STATUS_PAUSED);
            } else {
                setGameStatus(STATUS_PLAYING);
            }
        });
        // Начинаем игру со стартового статуса
        setGameStatus(STATUS_START);
    }

    @Override
    protected void onPause() {
        // При переходе Activity в состояние паузы, если игра была в процессе, ставим её на паузу
        super.onPause();
        if (mGameStatus.get() == STATUS_PLAYING) {
            setGameStatus(STATUS_PAUSED);
        }
    }

    private void setGameStatus(int gameStatus) {
        // Получаем предыдущий статус для возможности проверки изменился ли он
        int prevStatus = mGameStatus.get();
        mGameStatusText.setVisibility(View.VISIBLE);
        // Устанавливаем текст кнопки управления игрой по умолчанию на "Старт"
        mGameBtn.setText("Старт");
        // Обновляем текущий статус игры
        mGameStatus.set(gameStatus);
        // Выполняем действия на основе нового статуса игры
        switch (gameStatus) {
            case STATUS_OVER:
                // Если игра закончена, отображаем соответствующее сообщение
                mGameStatusText.setText("ИГРА ЗАКОНЧЕНА");
                break;
            case STATUS_START:
                // При стартовом статусе инициируем новую игру
                mGameView.newGame();
                mGameStatusText.setText("НАЖМИТЕ НА СТАРТ");
                break;
            case STATUS_PAUSED:
                // Если игра на паузе, информируем об этом игрока
                mGameStatusText.setText("ИГРА НА ПАУЗЕ");
                break;
            case STATUS_PLAYING:
                // При переходе в статус игры начинаем игру
                if (prevStatus == STATUS_OVER) {
                    // Если предыдущий статус был "Игра закончена", начинаем новую игру
                    mGameView.newGame();
                }
                // Запускаем игровой процесс
                startGame();
                // Прячем текст статуса игры
                mGameStatusText.setVisibility(View.INVISIBLE);
                // Меняем текст кнопки управления на "Пауза"
                mGameBtn.setText("Пауза");
                break;
        }
    }

    private void startGame() {
        // Рассчитываем задержку для достижения необходимого FPS
        final int delay = 1000 / FPS;
        // Запускаем игровой цикл в отдельном потоке
        new Thread(() -> {
            int count = 0;
            // Циклически запускаем обновление игрового поля до тех пор, пока игра не закончится и не будет поставлена на паузу
            while (!mGameView.isGameOver() && mGameStatus.get() != STATUS_PAUSED) {
                try {
                    // Замедляем поток, чтобы соответствовать заданному FPS
                    Thread.sleep(delay);
                    if (count % SPEED == 0) {
                        // Обновляем игровое поле и делаем его перерисовку
                        mGameView.next();
                        // Метод post обеспечивает выполнение действия invalidate в основном потоке UI
                        mHandler.post(mGameView::invalidate);
                    }
                    count++;
                } catch (InterruptedException ignored) {
                    // В случае прерывания потока игнорируем его и продолжаем дальше
                }
            }
            // Если игра закончилась, устанавливаем соответствующий статус
            if (mGameView.isGameOver()) {
                mHandler.post(() -> setGameStatus(STATUS_OVER));
            }
        }).start();
    }
}