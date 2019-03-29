package ca.cmpt276theta.sudokuvocabulary.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import ca.cmpt276theta.sudokuvocabulary.R;
import ca.cmpt276theta.sudokuvocabulary.controller.Word;
import ca.cmpt276theta.sudokuvocabulary.model.GameData;
import ca.cmpt276theta.sudokuvocabulary.model.GameDataGenerator;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        loadGame();
        TextView textView = findViewById(R.id.name);
        String str = "<font color='#0373D6'>S</font>udoku  <font color='#0373D6'>V</font>ocabulary";
        textView.setText(Html.fromHtml(str));
        final Animation animation1 = AnimationUtils.loadAnimation(this, R.anim.launch_page_part1_animation);
        final ImageView logoPart1 = findViewById(R.id.logo_1);
        final Animation animation2 = AnimationUtils.loadAnimation(this, R.anim.launch_page_part2_animation);
        final ImageView logoPart2 = findViewById(R.id.logo_2);
        final Animation animation3 = AnimationUtils.loadAnimation(this, R.anim.launch_page_part3_animation);
        final ImageView logo = findViewById(R.id.logo);
        logoPart1.startAnimation(animation1);
        logoPart2.startAnimation(animation2);
        animation3.setStartOffset(700);
        logo.startAnimation(animation3);
        textView.startAnimation(animation3);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(LaunchActivity.this, MainMenuActivity.class));
                finish();
            }
        }, 3200);
    }

    private void loadGame (){
        GameDataGenerator.loadPuzzleData();
        GameData.setWordlist(new ArrayList<Word>());
        loadArray(GameData.getWordlist());
        GameData.sortWordData();
    }

    private void loadArray(ArrayList<Word> list) {
        SharedPreferences mSharedPreference1 = this.getSharedPreferences("wordList", MODE_PRIVATE);
        list.clear();
        int size = mSharedPreference1.getInt("Size", 0);
        for(int i = 0; i < size; i++) {
            Word word = new Word();
            word.setEnglish(mSharedPreference1.getString("Status_English" + i, null));
            word.setFrench(mSharedPreference1.getString("Status_French" + i, null));
            word.setScore(mSharedPreference1.getInt("Status_Score" + i,0));
            list.add(word);
        }
    }
}