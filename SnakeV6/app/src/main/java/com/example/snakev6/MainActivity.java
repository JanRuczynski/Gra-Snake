package com.example.snakev6;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {

    private static final int FPS = 60;
    private static final int SPEED = 35;

    private static final int STATUS_PAUSED = 1;
    private static final int STATUS_START = 2;
    private static final int STATUS_OVER = 3;
    private static final int STATUS_PLAYING = 4;

    private GameView GameView;
    private TextView GameStatusText;
    private TextView GameScoreText;
    private Button GameBtn;

    private final AtomicInteger GameStatus = new AtomicInteger(STATUS_START);

    private final Handler Handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GameView = findViewById(R.id.game_view);
        GameStatusText = findViewById(R.id.game_status);
        GameBtn = findViewById(R.id.game_control_btn);
        GameScoreText = findViewById(R.id.game_score);
        GameView.init();
        GameView.setGameScoreUpdatedListener(score -> {
            Handler.post(() -> GameScoreText.setText("Wynik: " + score));
        });

        findViewById(R.id.up_btn).setOnClickListener(v -> {
            if (GameStatus.get() == STATUS_PLAYING) {
                GameView.setDirection(Direction.UP);
            }
        });
        findViewById(R.id.down_btn).setOnClickListener(v -> {
            if (GameStatus.get() == STATUS_PLAYING) {
                GameView.setDirection(Direction.DOWN);
            }
        });
        findViewById(R.id.left_btn).setOnClickListener(v -> {
            if (GameStatus.get() == STATUS_PLAYING) {
                GameView.setDirection(Direction.LEFT);
            }
        });
        findViewById(R.id.right_btn).setOnClickListener(v -> {
            if (GameStatus.get() == STATUS_PLAYING) {
                GameView.setDirection(Direction.RIGHT);
            }
        });

        GameBtn.setOnClickListener(v -> {
            if (GameStatus.get() == STATUS_PLAYING) {
                setGameStatus(STATUS_PAUSED);
            } else {
                setGameStatus(STATUS_PLAYING);
            }
        });

        setGameStatus(STATUS_START);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (GameStatus.get() == STATUS_PLAYING) {
            setGameStatus(STATUS_PAUSED);
        }
    }

    private void setGameStatus(int gameStatus) {
        int prevStatus = GameStatus.get();
        GameStatusText.setVisibility(View.VISIBLE);
        GameBtn.setText("start");
        GameStatus.set(gameStatus);
        switch (gameStatus) {
            case STATUS_OVER:
                GameStatusText.setText("KONIEC GRY!");
                break;
            case STATUS_START:
                GameView.newGame();
                GameStatusText.setText("ROZPOCZNIJ GRE");
                break;
            case STATUS_PAUSED:
                GameStatusText.setText("GRA ZAPAUZOWANA");
                break;
            case STATUS_PLAYING:
                if (prevStatus == STATUS_OVER) {
                    GameView.newGame();
                }
                startGame();
                GameStatusText.setVisibility(View.INVISIBLE);
                GameBtn.setText("pauza");
                break;
        }
    }

    private void startGame() {
        final int delay = 1000 / FPS;
        new Thread(() -> {
            int count = 0;
            while (!GameView.isGameOver() && GameStatus.get() != STATUS_PAUSED) {
                try {
                    Thread.sleep(delay);
                    if (count % SPEED == 0) {
                        GameView.next();
                        Handler.post(GameView::invalidate);
                    }
                    count++;
                } catch (InterruptedException ignored) {
                }
            }
            if (GameView.isGameOver()) {
                Handler.post(() -> setGameStatus(STATUS_OVER));
            }
        }).start();
    }
}