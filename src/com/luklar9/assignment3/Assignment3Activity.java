package com.luklar9.assignment3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Assignment3Activity extends Activity
{
    /** Called when the activity is first created. */

    static WindowManager wm;
    public float touched_x;
    public static TextView midScreenTV;
    private EditText nameET;


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start);
        wm = getWindowManager();

        nameET = (EditText)findViewById(R.id.name);
        nameET.setOnKeyListener(onSoftKeyboardDonePress);



    }

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
        midScreenTV = (TextView) findViewById(R.id.text3);
        midScreenTV.setText(" ");
        SQLiteHandler.setName("" + nameET.getText());
        CanvasThread.restart();
        return true;
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_start:
                CanvasThread.pause();
                break;
            case R.id.menu_restart:
                Log.w("asdf", "asdf");
                break;
        }
        return true;
    }


    public static void updateDM (DisplayMetrics dm){
    wm.getDefaultDisplay().getMetrics(dm);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

}