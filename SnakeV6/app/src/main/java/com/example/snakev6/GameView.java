package com.example.snakev6;

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
    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private static final String TAG = "GameView";

    private static final int MAP_SIZE = 20;
    private static final int START_X = 5;
    private static final int START_Y = 10;

    private final Point[][] Points = new Point[MAP_SIZE][MAP_SIZE];
    private final LinkedList<Point> Snake = new LinkedList<>();
    private Direction Dir;

    private ScoreUpdatedListener ScoreUpdatedListener;

    private boolean GameOver = false;

    private int BoxSize;
    private int BoxPadding;

    private final Paint Paint = new Paint();

    public void init() {
        BoxSize = getContext().getResources()
                .getDimensionPixelSize(R.dimen.game_size) / MAP_SIZE;
        BoxPadding = BoxSize / 10;
    }

    public void newGame() {
        GameOver = false;
        Dir = Direction.RIGHT;
        initMap();
        updateScore();
    }

    public void setGameScoreUpdatedListener(ScoreUpdatedListener scoreUpdatedListener) {
        ScoreUpdatedListener = scoreUpdatedListener;
    }

    private void initMap() {
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                Points[i][j] = new Point(j, i);
            }
        }
        Snake.clear();
        for (int i = 0; i < 3; i++) {
            Point point = getPoint(START_X + i, START_Y);
            point.type = PointType.SNAKE;
            Snake.addFirst(point);
        }
        randomApple();
    }

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
        return Points[y][x];
    }

    public void next() {
        Point first = Snake.getFirst();
        Log.d(TAG, "first: " + first.x + " " + first.y);
        Point next = getNext(first);
        Log.d(TAG, "next: " + next.x + " " + next.y);

        switch (next.type) {
            case EMPTY:
                Log.d(TAG, "next: empty");
                next.type = PointType.SNAKE;
                Snake.addFirst(next);
                Snake.getLast().type = PointType.EMPTY;
                Snake.removeLast();
                break;
            case APPLE:
                Log.d(TAG, "next: apple");
                next.type = PointType.SNAKE;
                Snake.addFirst(next);
                randomApple();
                updateScore();
                break;
            case SNAKE:
                Log.d(TAG, "next: snake");
                GameOver = true;
                break;
        }
    }

    public void updateScore() {
        if (ScoreUpdatedListener != null) {
            int score = Snake.size() - 3;
            ScoreUpdatedListener.onScoreUpdated(score);
        }
    }

    public void setDirection(Direction dir) {
        if ((dir == Direction.LEFT || dir == Direction.RIGHT) &&
                (Dir == Direction.LEFT || Dir == Direction.RIGHT)) {
            return;
        }
        if ((dir == Direction.UP || dir == Direction.DOWN) &&
                (Dir == Direction.UP || Dir == Direction.DOWN)) {
            return;
        }
        Dir = dir;
    }

    private Point getNext(Point point) {
        int x = point.x;
        int y = point.y;

        switch (Dir) {
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

    public boolean isGameOver() {
        return GameOver;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int y = 0; y < MAP_SIZE; y++) {
            for (int x = 0; x < MAP_SIZE; x++) {
                int left = BoxSize * x;
                int right = left + BoxSize;
                int top = BoxSize * y;
                int bottom = top + BoxSize;
                switch (getPoint(x, y).type) {
                    case APPLE:
                        Paint.setColor(Color.RED);
                        break;
                    case SNAKE:
                        Paint.setColor(Color.BLACK);
                        canvas.drawRect(left, top, right, bottom, Paint);
                        Paint.setColor(Color.GREEN);
                        left += BoxPadding;
                        right -= BoxPadding;
                        top += BoxPadding;
                        bottom -= BoxPadding;
                        break;
                    case EMPTY:
                        Paint.setColor(Color.BLACK);
                        break;
                }
                canvas.drawRect(left, top, right, bottom, Paint);
            }
        }
    }
}