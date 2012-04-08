package com.luklar9.assignment3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.TextView;

public class GameOver extends Activity {
    /** Called when the activity is first created. */
    private String nameContainer = "";

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gameover);
        TextView scoreTV = (TextView) findViewById(R.id.gameover2);
        TextView winningTV = (TextView) findViewById(R.id.gameover);
        Intent i = getIntent();

        if (!SQLiteHandler.getName().equals(nameContainer)) {
            nameContainer =  " " + SQLiteHandler.getName() + ",";
        }

        if (i.getExtras().get("winning").toString().equals("yup")) {
            winningTV.setText("Dang," + nameContainer + " you won!");
        } else {
            winningTV.setText("Game over," + nameContainer + " :/");
        }
        scoreTV.setText("Score: " + i.getExtras().get("score").toString());


    }

}
