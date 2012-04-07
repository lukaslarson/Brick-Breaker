package com.luklar9.assignment3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.TextView;

/**
 * Created with IntelliJ IDEA.
 * User: Chokis
 * Date: 2012-04-04
 * Time: 17:19
 * To change this template use File | Settings | File Templates.
 */
public class GameOver extends Activity {
    /** Called when the activity is first created. */


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameover);
        TextView scoreTV = (TextView) findViewById(R.id.gameover2);
        TextView winningTV = (TextView) findViewById(R.id.gameover);
        Intent i = getIntent();

        if (i.getExtras().get("winning").toString().equals("yup")) {
            winningTV.setText("Dang, " + SQLiteHandler.getName() + ", you won!");
        } else {
            winningTV.setText("Game over :(");
        }
        scoreTV.setText("Score: " + i.getExtras().get("score").toString());


    }

}
