package com.luklar9.assignment3;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity
{
    /** Called when the activity is first created. */

    private static WindowManager wm;
    private static TextView livesTV;
    private static TextView scoreTV;
    private EditText nameET;

    // handler to update textviews from other thread
    public static Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int lives = msg.getData().getInt("lives");
            int score = msg.getData().getInt("score");

            livesTV.setText("Remaining lives: " + lives);
            scoreTV.setText("Score: " + score);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        wm = getWindowManager();

        nameET = (EditText)findViewById(R.id.name);
        nameET.setOnKeyListener(onSoftKeyboardDonePress);
    }

    // fix DONE button on the text input
    private View.OnKeyListener onSoftKeyboardDonePress=new View.OnKeyListener()
    {
        public boolean onKey(View v, int keyCode, KeyEvent event)
        {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)
            {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.toggleSoftInput(0, 0);
            }
            return false;
        }
    };

    @Override
    public boolean onTouchEvent( MotionEvent e )
    {
        setContentView(R.layout.main);
        livesTV = (TextView) findViewById(R.id.lives);
        scoreTV = (TextView) findViewById(R.id.score);
        SQLiteHandler.setName("" + nameET.getText());

        // run it
        CanvasThread.justStart();
        return true;
    }

    // a menu to pause and... yeah. pause.
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_start:
                CanvasThread.pause();
                break;
            case R.id.menu_restart:
                //don't do anything just yet..
                break;
        }
        return true;
    }

    // update the lives TextView from the canvasthread
    public static void updateLives (int lives) {
        livesTV.setText("Remaining lives: " + lives);
    }

    // update the score TextView from the canvasthread
    public static void updateScore (int score) {
        scoreTV.setText("Score: " + score);
    }

    // get screen size
    public static void updateDM (DisplayMetrics dm){
        wm.getDefaultDisplay().getMetrics(dm);
    }

    // set the menu
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
}