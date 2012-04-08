package com.luklar9.assignment3;

import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.*;

import java.util.HashMap;
import java.util.Random;

public class Panel extends SurfaceView implements SurfaceHolder.Callback {

    private float touched_x = 0;
    // private float touched_y = 0; not used yet
    private float screen_x = 0;
    private float screen_y = 0;
    private float[] ball = new float[4]; // x-pos, y-pos, x-speed, y-speed
    private Random random = new Random();
    private static DisplayMetrics metrics = new DisplayMetrics();
    private Brick[] bricks;
    private Bitmap brickBM = BitmapFactory.decodeResource(getResources(),R.drawable.brick);
    private Paint paint = new Paint();
    private Rect ballRect;
    private Rect paddleRect;
    private CanvasThread canvasthread;
    private int lives;
    private int score;
    private boolean isResumed = false;
    private float xSpeed = 4.0f;
    private float ySpeed = -8.0f;
    private SoundManager soundManager = new SoundManager();
    private int numCols = 4; // more than 20 is too thin
    private int numRows = 3; // more than 20 is too high
    private int brickHeight = 10;
    private int brickYSeparator = 25;
    private float brickWidth;
    private boolean hasCollided;

    @Override
    public boolean onTouchEvent( MotionEvent e )
    {
        // get the finger x-position to update the paddle every onDraw
        touched_x = e.getX();
        //touched_y = e.getY();

        return true;
    }

    public Panel(Context context, AttributeSet attributeSet) {
        super(context);
        getHolder().addCallback(this);
        canvasthread = new CanvasThread(getHolder(), this);
        setFocusable(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        // update screen size
        MainActivity.updateDM(metrics);
        screen_x = metrics.widthPixels;
        screen_y = metrics.heightPixels;

        // initiate the sound manager
        soundManager.initSounds(getContext());
        soundManager.addSound(1, R.raw.brickbounce);
        soundManager.addSound(2, R.raw.paddlebounce);
        soundManager.addSound(3, R.raw.wallbounce);

        // set all values anew if the game is not resumed
        if (!isResumed) {
            startGame();
            isResumed = true;
        }

        // check if thread is terminated and start it
        if(canvasthread.getState()== Thread.State.TERMINATED){
            canvasthread = new CanvasThread(getHolder(), this);
            canvasthread.setRunning(true);
            canvasthread.start();
        }else {
            canvasthread.setRunning(true);
            canvasthread.start();
        }
        CanvasThread.justStart();
    }

    void startGame() {

        // set some values
        bricks = new Brick[numCols*numRows];
        ball[0] = (int) screen_x / 2;
        ball[1] = (int) screen_y - 385;
        ball[2] = random.nextFloat() * xSpeed;
        ball[3] = ySpeed;
        score = 0;
        lives = 3;

        // generate bricks
        brickWidth = ( screen_x / numCols) * 3/4;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                bricks[i*numCols + j] = new Brick((int)(brickWidth/3*(j+0.5)) + brickWidth*j, brickYSeparator*(i+1),
                        (int)(brickWidth/3*(j+0.5)) + brickWidth*j + brickWidth, brickYSeparator*(i+1) + brickHeight);
            }
        }

        updateScore();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

        // screen rotation is locked in this version
        MainActivity.updateDM(metrics);
        screen_x = metrics.widthPixels;
        screen_y = metrics.heightPixels;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        boolean retry = true;
        canvasthread.setRunning(false);
        while(retry) {
            try {
                // keep trying until the join works
                canvasthread.join();
                retry = false;
            }   catch (InterruptedException e) {
                    // do nothing
                }
        }
    }

    //@Override
    public void onDraw(Canvas canvas) {
        // draw everything black
        canvas.drawColor(Color.BLACK);

        drawBricks(canvas);
        drawBall(canvas);
        drawPaddle(canvas);
        detectCollisions();
        //drawBackground(canvas); we'll just save this..
    }

    void drawBricks(Canvas canvas) {
        for (int j = 0; j < bricks.length; j++) {
            if (bricks[j].isAlive) {
                //canvas.drawBitmap(brickBM, bricks[j].x1, bricks[j].y1, paint);
                canvas.drawBitmap(brickBM, new Rect(0,0,40,10), bricks[j].rect, paint);
            } else {
                // if they should fade
                if (bricks[j].fade) {
                    if (bricks[j].alpha > 2) {
                        // start drawing them while decreasing their alpha
                        bricks[j].alpha -= 2;
                        paint.setAlpha(bricks[j].alpha);
                        canvas.drawBitmap(brickBM, new Rect(0,0,40,10), bricks[j].rect, paint);
                    } else {
                        // stop when alpha is 1, 0 draws opaque
                        bricks[j].fade = false;
                    }
                }
                paint.setAlpha(255);
            }
        }
    }

    void drawBall(Canvas canvas) {
        paint.setColor(Color.WHITE);
        // make the ball move with every draw
        ball[0] = ball[0] + ball[2];
        ball[1] = ball[1] + ball[3];
        // create a rect from ball[0] and ball[1] and draw it
        ballRect = new Rect((int) ball[0], (int) ball[1], (int) ball[0] + 15, (int) ball[1] + 15);
        canvas.drawRect(ballRect,paint);
    }

    void drawPaddle(Canvas canvas) {
        paint.setColor(Color.YELLOW);
        // stop the paddle from going through the screen edges
        if (touched_x < 75) {
            paddleRect = new Rect(0, (int) screen_y - 220, 150, (int) screen_y - 190);
        } else if (touched_x + 75 > screen_x) {
            paddleRect = new Rect( (int)screen_x - 150,  (int)screen_y - 220,  (int)screen_x,  (int)screen_y - 190);
        } else {
            paddleRect = new Rect( (int)touched_x - 75,  (int)screen_y - 220,  (int)touched_x + 75,  (int)screen_y - 190);
        }
        // draw it
        canvas.drawRect(paddleRect, paint);
    }

    void detectCollisions() {

        // fix for multiple collisions reinverting course
        hasCollided = false;
        // if collides with x-edges invert x-speed
        if (ball[0] + ball[2] + 15>= screen_x || ball[0] + ball[2] <= 0) {
            ball[2] = ball[2] * -1;
            soundManager.play(3);
        }

        // if collides with top screen edge invert y-speed
        if (ball[1] + ball[3] <= 0) {
            ball[3] = ball[3] * -1.0f;
            soundManager.play(3);
        }

        // if the ball and paddle intersect reset y-speed and calc x-speed
        if (Rect.intersects(ballRect, paddleRect)) {
            soundManager.play(2);
            ball[3] = ySpeed;
            // x speed (ball[2]) equals
            // far left -> -1 * xSpeed; middle -> 0 * xSpeed; far right -> 1 * xSpeed
            ball[2] = ((((ballRect.left + ballRect.right)/2.0f) - ((paddleRect.left + paddleRect.right) /2.0f)) / 75.0f * xSpeed);
        }

        // if misses paddle
        if (ball[1] >= screen_y) {
            death(getContext(), false);
        }

        // check intersects with ball and living bricks
        for (int j = 0; j < bricks.length; j++) {
            if (bricks[j].isAlive) {
                // if intersects invert y-speed and start the fade
                if (Rect.intersects(bricks[j].rect, ballRect)) {
                    // fix for multiple collisions reinverting course
                    if (!hasCollided) {
                        ball[3] = ball[3] * -1;
                        hasCollided = true;
                    }
                    score += 1;
                    updateScore();

                    soundManager.play(1);
                    bricks[j].isAlive = false;
                    bricks[j].fade = true;

                    // all bricks destroyed, player wins!
                    if (score == bricks.length) {
                        death(getContext(), true);
                    }
                }
            }
        }
    }

    // random a pixel and color and draw it (not used since we're redrawing black over the canvas each ondraw)
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

    void death(Context context, boolean wonGame) {
        // if all bricks are gone
        if (wonGame) {
            // pause the thread and set it to reset on next surfacecreated
            isResumed = false;
            CanvasThread.pause();

            // start the game over activity
            Intent i = new Intent();
            i.setClassName("com.luklar9.assignment3", "com.luklar9.assignment3.GameOver");
            i.putExtra("score", score);
            i.putExtra("winning", "yup");
            SQLiteHandler.setScore(score);
            context.startActivity(i);
        }

        // regular death
        else {

            // if has remaining lives
            if (lives > 1) {
                // decrease lives
                lives -=1;
                updateScore();

                // reset ball position
                ball[0] = (int) screen_x / 2;
                ball[1] = (int) screen_y - 385;
                ball[2] = random.nextFloat() * xSpeed;
                ball[3] = ySpeed;

            } else {

                // pause the thread and set it to reset on next surfacecreated
                isResumed = false;
                CanvasThread.pause();

                // start game over activity
                Intent i = new Intent();
                i.setClassName("com.luklar9.assignment3", "com.luklar9.assignment3.GameOver");
                i.putExtra("score", score);
                i.putExtra("winning", "nop");
                context.startActivity(i);
            }
        }
    }

    // updates the textviews above the surfaceview panel
    public void updateScore() {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putInt("score", score);
        bundle.putInt("lives", lives);
        msg.setData(bundle);
        MainActivity.handler.sendMessage(msg);
    }
}

class Brick {
    private float x1;
    private float y1;
    private float x2;
    private float y2;
    public boolean isAlive = false;
    public Rect rect;
    public boolean fade = false;
    public int alpha = 255;

    // set the brick alive when constructed
    public Brick (float px1, float py1, float px2, float py2) {
        x1 = px1;
        y1 = py1;
        x2 = px2;
        y2 = py2;
        isAlive = true;
        rect = new Rect((int) x1, (int) y1, (int) x2, (int) y2);
    }
}

class SoundManager {
    private  SoundPool soundPool;
    private  HashMap soundPoolMap;
    private  AudioManager  audioManager;
    private  Context context;

    // initiate
    public void initSounds(Context c) {
        context = c;
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        soundPoolMap = new HashMap();
        audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
    }

    // add sound from .raw into the map
    public void addSound(int index, int SoundID)
    {
        soundPoolMap.put(index, soundPool.load(context, SoundID, 1));
    }

    // set the volume and play the sound
    public void play(int sound) {
        float streamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        streamVolume = streamVolume / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        soundPool.play((Integer) soundPoolMap.get(sound), streamVolume, streamVolume, 1, 0, 1f);
    }
}
