package com.luklar9.assignment3;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

/**
 * Created by IntelliJ IDEA.
 * User: Chokis
 * Date: 2012-03-27
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class CanvasThread extends Thread {
    public static SurfaceHolder _surfaceHolder;
    private Panel _panel;
    public boolean _run = false;
    private static boolean running = false;

    public CanvasThread(SurfaceHolder surfaceHolder, Panel panel) {
        _surfaceHolder = surfaceHolder;
        _panel = panel;

    }

    public void setRunning(boolean run) {
        _run = run;
    }

    public static void pause() {
        synchronized (_surfaceHolder) {
            if (running)  {
                running = false;
            }
           else {
            running = true;
        }
    }}

    public static void restart() {
        synchronized (_surfaceHolder) {

                running = true;

        }}

    @Override
    public void run() {
        Canvas c;
        int i = 0;
        while (_run) {
            c = null;
            try {
                if (i<250) {
                    i++;
                }
                c = _surfaceHolder.lockCanvas(null);
                synchronized (_surfaceHolder) {
                    if (running) {
                        _panel.onDraw(c, i);
                    }

                }
            }   finally {
                if (c != null) {
                    _surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }

}
