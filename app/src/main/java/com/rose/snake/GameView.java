package com.rose.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.Random;

public class GameView extends View {
    // Конструкторы View, используемые при создании из кода или разметки XML со стилями и атрибутами
    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    // Тэг для логирования - помогает идентифицировать сообщения в журнале
    private static final String TAG = "GameView";
    // Константы игровой логики
    private static final int MAP_SIZE = 20; // Размер игрового поля в клетках
    private static final int START_X = 5; // Начальное положение головы змеи (по горизонтали)
    private static final int START_Y = 10; // Начальное положение головы змеи (по вертикали)

    // Игровой массив клеток и список для представления змеи
    private final Point[][] mPoints = new Point[MAP_SIZE][MAP_SIZE];
    private final LinkedList<Point> mSnake = new LinkedList<>();
    // Текущее направление движения змеи
    private Direction mDir;
    // Слушатель для обратных вызовов обновления счета
    private ScoreUpdatedListener mScoreUpdatedListener;
    // Флаг, указывающий на то, закончена игра или нет
    private boolean mGameOver = false;
    // Размеры каждой клетки и отступ внутри неё
    private int mBoxSize;
    private int mBoxPadding;
    // Кисть для рисования элементов на канве
    private final Paint mPaint = new Paint();
    // Инициализация View и вычисление необходимых размеров клеток
    public void init() {
        // Определяем размер клетки на основе размеров игровой области из ресурсов
        mBoxSize = getContext().getResources()
                .getDimensionPixelSize(R.dimen.game_size) / MAP_SIZE;
        // Рассчитываем отступ внутри клетки
        mBoxPadding = mBoxSize / 10;
    }

    // Начинаем новую игру, инициализируя игровое поле и обновляя счет
    public void newGame() {
        mGameOver = false; // Ставим флаг, что игра не закончена
        mDir = Direction.RIGHT; // Направление движения змеи в начале игры - вправо
        initMap(); // Инициализируем карту
        updateScore(); // Обновляем счет
    }

    // Устанавливаем слушатель для обработки обновлений счета
    public void setGameScoreUpdatedListener(ScoreUpdatedListener scoreUpdatedListener) {
        mScoreUpdatedListener = scoreUpdatedListener;
    }

    // Инициализация карты и всех игровых объектов: змея и яблоки
    private void initMap() {
        // Заполнение карты точками и очистка списка змеи
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                mPoints[i][j] = new Point(j, i);
            }
        }
        mSnake.clear(); // Очищаем список змеи
        // Создаем начальную змею с 3 сегментами в заданной начальной позиции
        for (int i = 0; i < 3; i++) {
            Point point = getPoint(START_X + i, START_Y);
            point.type = PointType.SNAKE;
            mSnake.addFirst(point);
        }
        randomApple(); // Случайно добавляем яблоко на карту
    }

    // принцип работы рандома (яблоко)
    private void randomApple() {
        Random random = new Random();
        while (true) {
            Point point = getPoint(random.nextInt(MAP_SIZE),
                    random.nextInt(MAP_SIZE));
            if (point.type == PointType.EMPTY) {
                point.type = PointType.APPLE;
                break;
            }
        }
    }

    private Point getPoint(int x, int y) {
        return mPoints[y][x];
    }

    public void next() {
        Point first = mSnake.getFirst();
        Log.d(TAG, "first: " + first.x + " " + first.y);
        Point next = getNext(first);
        Log.d(TAG, "next: " + next.x + " " + next.y);

        switch (next.type) {
            case EMPTY:
                Log.d(TAG, "next: empty");
                next.type = PointType.SNAKE;
                mSnake.addFirst(next);
                mSnake.getLast().type = PointType.EMPTY;
                mSnake.removeLast();
                break;
            case APPLE:
                Log.d(TAG, "next: apple");
                next.type = PointType.SNAKE;
                mSnake.addFirst(next);
                randomApple();
                updateScore();
                break;
            case SNAKE:
                Log.d(TAG, "next: snake");
                mGameOver = true;
                break;
        }
    }

    // обновление счетчика
    public void updateScore() {
        if (mScoreUpdatedListener != null) {
            int score = mSnake.size() - 3;
            mScoreUpdatedListener.onScoreUpdated(score);
        }
    }

    public void setDirection(Direction dir) {
        // Устанавливает новое направление движения змеи, но с ограничениями:
        // Запрещает изменять направление на противоположное во избежание "въезда в себя"
        // Если направление движения уже горизонтальное и новое тоже горизонтальное - игнорируем изменение
        if ((dir == Direction.LEFT || dir == Direction.RIGHT) &&
                (mDir == Direction.LEFT || mDir == Direction.RIGHT)) {
            return; // Не меняем направление, если оно не приводит к изменению оси
        }
        // Если направление движения уже вертикальное и новое тоже вертикальное - тоже игнорируем
        if ((dir == Direction.UP || dir == Direction.DOWN) &&
                (mDir == Direction.UP || mDir == Direction.DOWN)) {
            return;
        }
        // Устанавливаем новое направление только в случае, если оно меняет ось движения
        mDir = dir;
    }

    // Получение продолжение
    private Point getNext(Point point) {
        int x = point.x;
        int y = point.y;

        switch (mDir) {
            case UP:
                y = y == 0 ? MAP_SIZE - 1 : y - 1;
                break;
            case DOWN:
                y = y == MAP_SIZE - 1 ? 0 : y + 1;
                break;
            case LEFT:
                x = x == 0 ? MAP_SIZE - 1 : x - 1;
                break;
            case RIGHT:
                x = x == MAP_SIZE - 1 ? 0 : x + 1;
                break;
        }
        return getPoint(x, y);
    }

    // Проверяем, закончилась ли игра.
    public boolean isGameOver() {
        return mGameOver;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Встроенный метод View, вызывается при перерисовке компонента
        super.onDraw(canvas);
        // Проходим по каждой ячейке игрового поля
        for (int y = 0; y < MAP_SIZE; y++) {
            // Проходим по каждой ячейке игрового поля
            for (int x = 0; x < MAP_SIZE; x++) {
                // Вычисляем координаты для рисования клеток
                int left = mBoxSize * x;
                int right = left + mBoxSize;
                int top = mBoxSize * y;
                int bottom = top + mBoxSize;
                // Определяем, что находится в текущей клетке, и выбираем цвет для её отрисовки
                switch (getPoint(x, y).type) {
                    case APPLE:
                        // Если в ячейке яблоко, рисуем его красным цветом
                        mPaint.setColor(Color.RED);
                        break;
                    case SNAKE:
                        // Если ячейка принадлежит змее, рисуем её чёрным цветом
                        mPaint.setColor(Color.BLACK);
                        canvas.drawRect(left, top, right, bottom, mPaint);
                        // Добавляем белую рамку внутри черных ячеек для визуального различия сегментов змеи
                        mPaint.setColor(Color.WHITE);
                        left += mBoxPadding;
                        right -= mBoxPadding;
                        top += mBoxPadding;
                        bottom -= mBoxPadding;
                        break;
                    case EMPTY:
                        // Если ячейка пустая, рисуем её чёрным цветом (или устанавливаем другой цвет фона)
                        mPaint.setColor(Color.BLACK);
                        break;
                }
                // Рисуем прямоугольники (клетки игрового поля) с установленным цветом
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        }
    }
}
