package com.luklar9.assignment3;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.*;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Chokis
 * Date: 2012-03-27
 * Time: 14:38
 * To change this template use File | Settings | File Templates.
 */
public class Panel extends SurfaceView implements SurfaceHolder.Callback {

    private float touched_x = 0;
    private float touched_y = 0;
    private float screen_x = 0;
    private float screen_y = 0;
    private float[] ball = new float[4];
    private Random random = new Random();
    public static DisplayMetrics metrics = new DisplayMetrics();
    private Brick[] bricks = new Brick[18];
    public boolean drawAgain = false;
    public int brick = 0;
    public Bitmap brickBM = BitmapFactory.decodeResource(getResources(),R.drawable.brick);
    Animation fadeoutAnim = AnimationUtils.loadAnimation(this.getContext(), R.anim.fadeout);
    private int alpha = 250;
    public Paint paint = new Paint();
    public Rect ballRect;
    public Rect paddelRect;
    public Rect test1 = new Rect(1, 1, 15, 15);
    public Rect test2 = new Rect(3, 3, 17, 17);
    public CanvasThread canvasthread;
    public int lives;
    public int score;
    public boolean isResumed = false;
    public float xSpeed = 4.0f;
    public float ySpeed = -8.0f;
    SoundManager soundManager = new SoundManager();


    @Override
    public boolean onTouchEvent( MotionEvent e )
    {
        touched_x = e.getX();
        touched_y = e.getY();

        return true;
    }


    //@Override

    public Panel(Context context, AttributeSet attributeSet) {
        super(context);
        getHolder().addCallback(this);
        canvasthread = new CanvasThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Assignment3Activity.updateDM(metrics);
        screen_x = metrics.widthPixels;
        screen_y = metrics.heightPixels;
        Log.w("PIXLAR!!", "" + screen_x + " + " + screen_y);

        soundManager.initSounds(getContext());
        soundManager.addSound(1, R.raw.brickbounce);
        soundManager.addSound(2, R.raw.paddlebounce);
        soundManager.addSound(3, R.raw.wallbounce);

        if (!isResumed) {
            startGame();
            isResumed = true;
        }


        if(canvasthread.getState()== Thread.State.TERMINATED){
            canvasthread = new CanvasThread(getHolder(), this);
            canvasthread.setRunning(true);
            canvasthread.start();
            // <-- added fix
        }else {
            canvasthread.setRunning(true);
            canvasthread.start();
        }
        CanvasThread.restart();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //To change body of implemented methods use File | Settings | File Templates.
        Assignment3Activity.updateDM(metrics);
        screen_x = metrics.widthPixels;
        screen_y = metrics.heightPixels;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        canvasthread.setRunning(false);
        while(retry) {
            try {
                canvasthread.join();
                retry = false;
            }   catch (InterruptedException e) {
                // trytrytry
            }
        }

    }

    //@Override
    public void onDraw(Canvas canvas, int i) {
        canvas.drawColor(Color.BLACK);

        paint.setColor(Color.BLACK);

        for (int j = 0; j < bricks.length; j++) {

            if (bricks[j].drawAgain) {
                if (bricks[j].alpha > 2) {
                    bricks[j].alpha -= 2;
                    paint.setAlpha(bricks[j].alpha);
                    canvas.drawBitmap(brickBM, new Rect(0,0,40,10), bricks[j].brickRect, paint);
                } else {
                    bricks[j].drawAgain = false;
                }
            }

        }



        paint.setAlpha(255);
        //drawBackground(canvas);
        drawBricks(canvas);
        drawBall(canvas);
        drawPaddle(canvas);
        detectCollisions(canvas);
    }

    public void drawPaddle(Canvas canvas) {
        paint.setColor(Color.YELLOW);
        if (touched_x < 75) {
            paddelRect = new Rect(0, (int) screen_y - 220, (int) 150, (int) screen_y - 190);
        } else if (touched_x + 75 > screen_x) {
            paddelRect = new Rect( (int)screen_x - 150,  (int)screen_y - 220,  (int)screen_x,  (int)screen_y - 190);
        } else {
            paddelRect = new Rect( (int)touched_x - 75,  (int)screen_y - 220,  (int)touched_x + 75,  (int)screen_y - 190);
        }
        canvas.drawRect(paddelRect, paint);
    }


    public void drawBall(Canvas canvas) {
        paint.setColor(Color.WHITE);
        ball[0] = ball[0] + ball[2];
        ball[1] = ball[1] + ball[3];
        ballRect = new Rect((int) ball[0], (int) ball[1], (int) ball[0] + 15, (int) ball[1] + 15);
        canvas.drawRect(ballRect,paint);

    }

    public void startGame() {
        ball[0] = (int) screen_x / 2;
        ball[1] = (int) screen_y - 285;
        ball[2] = random.nextFloat() * xSpeed;
        ball[3] = ySpeed;
        score = 0;
        lives = 3;

        bricks[0] = new Brick(10,20,50,30);
        bricks[1] = new Brick(60,20,100,30);
        bricks[2] = new Brick(110,20,150,30);
        bricks[3] = new Brick(160,20,200,30);
        bricks[4] = new Brick(210,20,250,30);
        bricks[5] = new Brick(260,20,300,30);

        bricks[6] = new Brick(10,50,50,60);
        bricks[7] = new Brick(60,50,100,60);
        bricks[8] = new Brick(110,50,150,60);
        bricks[9] = new Brick(160,50,200,60);
        bricks[10] = new Brick(210,50,250,60);
        bricks[11] = new Brick(260,50,300,60);

        bricks[12] = new Brick(10,80,50,90);
        bricks[13] = new Brick(60,80,100,90);
        bricks[14] = new Brick(110,80,150,90);
        bricks[15] = new Brick(160,80,200,90);
        bricks[16] = new Brick(210,80,250,90);
        bricks[17] = new Brick(260,80,300,90);
    }

    public void drawBricks(Canvas canvas) {
        for (int j = 0; j < bricks.length; j++) {
            if (bricks[j].isAlive) {
                //canvas.drawBitmap(brickBM, bricks[j].x1, bricks[j].y1, paint);
                canvas.drawBitmap(brickBM, new Rect(0,0,40,10), bricks[j].brickRect, paint);
            }
        }
    }
    
    public void drawBackground (Canvas canvas) {
        int x = random.nextInt((int) screen_x);
        int y = random.nextInt((int) screen_y);
        int r = random.nextInt(255);
        int g = random.nextInt(255);
        int b = random.nextInt(255);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.rgb(r, g, b));
        canvas.drawPoint(x, y, paint);
    }

    public void detectCollisions(Canvas canvas) {
        if (ball[0] + ball[2] + 15>= screen_x || ball[0] + ball[2] <= 0) {
            ball[2] = ball[2] * -1;
            soundManager.play(3);

        }

        if (Rect.intersects(ballRect, paddelRect)) {
            soundManager.play(2);
            ball[3] = ySpeed;
            ball[2] = ((((ballRect.left + ballRect.right)/2.0f) - ((paddelRect.left + paddelRect.right) /2.0f)) / 75.0f * xSpeed);

            /*ball[3] = -2.5f + ball[2];   */

        }
        if (ball[1] + ball[3] <= 0) {
            ball[3] = ball[3] * -1.0f;
            soundManager.play(3);
        }

        // game over
        if (ball[1] >= screen_y) {
            death(getContext(), false);
        }

        for (int j = 0; j < bricks.length; j++) {
            if (bricks[j].detectCollision(ballRect)) {
                ball[3] = ball[3] * -1;
                score += 1;
                soundManager.play(1);

                // all bricks destroyed
                if (score == bricks.length) {
                    death(getContext(), true);
                }
            }

        }
    }

    public void death(Context context, boolean status) {
        if (status) {
            isResumed = false;
            CanvasThread.pause();

            Intent i = new Intent();
            i.setClassName("com.luklar9.assignment3", "com.luklar9.assignment3.GameOver");
            i.putExtra("score", score);
            i.putExtra("winning", "yup");
            SQLiteHandler.setScore(score);
            context.startActivity(i);
        } else {
            if (lives > 1) {
                lives -=1;

                ball[0] = (int) screen_x / 2;
                ball[1] = (int) screen_y - 285;
                ball[2] = random.nextFloat() * xSpeed;
                ball[3] = ySpeed;

            } else {
                isResumed = false;
                CanvasThread.pause();

                Intent i = new Intent();
                i.setClassName("com.luklar9.assignment3", "com.luklar9.assignment3.GameOver");
                i.putExtra("score", score);
                i.putExtra("winning", "nop");
                context.startActivity(i);
            }
        }

    }
}

class Brick {
    public float x1, y1, x2, y2;
    public boolean isAlive = false;
    public Rect brickRect;
    public boolean drawAgain = false;
    public int alpha = 255;

    public Brick (float px1, float py1, float px2, float py2) {
        x1 = px1;
        y1 = py1;
        x2 = px2;
        y2 = py2;
        isAlive = true;
        brickRect = new Rect((int) x1, (int) y1, (int) x2, (int) y2);
    }
    
    public boolean detectCollision(Rect rect) {
        if (isAlive) {
            if (Rect.intersects(rect, brickRect)) {
                isAlive = false;
                drawAgain = true;
                return true;
            }
        }
        return false;
    }

    public void fadeout() {

    }
}

class SoundManager {
    private  SoundPool soundPool;
    private  HashMap soundPoolMap;
    private  AudioManager  audioManager;
    private  Context context;

    public void initSounds(Context c) {
        context = c;
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        soundPoolMap = new HashMap();
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }


    public void addSound(int index, int SoundID)
    {
        soundPoolMap.put(index, soundPool.load(context, SoundID, 1));
    }

    public void play(int sound) {
        float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume = streamVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        soundPool.play((Integer) soundPoolMap.get(sound), streamVolume, streamVolume, 1, 0, 1f);
    }
}
