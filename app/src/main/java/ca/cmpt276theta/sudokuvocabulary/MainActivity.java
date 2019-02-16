package ca.cmpt276theta.sudokuvocabulary;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().setTitle("title");
        FrameLayout gameLayout = findViewById(R.id.gameLayout);
        GameView gameView = new GameView(this);
        Intent intent = getIntent();

        // Set Timer
        Chronometer timer = findViewById(R.id.chronometer1);
        timer.setBase(SystemClock.elapsedRealtime());
        timer.setFormat("Time: %s");
        timer.start();

        // initialize Victory Pop-up Window
        View popUpView = LayoutInflater.from(this).inflate(R.layout.victory_popup, null);
        final Intent intent2 = new Intent(this, MainMenu.class);
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        final PopupWindow pw = new PopupWindow(popUpView,size.x,size.y, false);
        pw.setClippingEnabled(false);
        pw.getContentView().findViewById(R.id.done).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                pw.dismiss();
                finish();
            }
        });
        final TextView time = pw.getContentView().findViewById(R.id.time);
        pw.setAnimationStyle(R.style.pop_animation);

        final GameMain gameMain = new GameMain(gameView, intent.getIntExtra("Mode", 1), pw, timer, time);
        gameView.setGameData(gameMain.getGameData());
        gameLayout.addView(gameView);

        // Set Buttons Bank
        final Button[] mButtons = new Button[9];
        mButtons[0] = findViewById(R.id.button0);
        mButtons[1] = findViewById(R.id.button1);
        mButtons[2] = findViewById(R.id.button2);
        mButtons[3] = findViewById(R.id.button3);
        mButtons[4] = findViewById(R.id.button4);
        mButtons[5] = findViewById(R.id.button5);
        mButtons[6] = findViewById(R.id.button6);
        mButtons[7] = findViewById(R.id.button7);
        mButtons[8] = findViewById(R.id.button8);

        // Set Listeners and Buttons' Text
        for (int i = 0; i < mButtons.length; i++) {
            final int j = i;
            mButtons[i].setText(gameMain.getGameData().getLanguageB(i).second);
            mButtons[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    gameMain.fillWord(mButtons[j]);
                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.game_top_menu, menu);
        return true;
    }

}
