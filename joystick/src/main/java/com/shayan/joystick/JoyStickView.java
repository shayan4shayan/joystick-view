package com.shayan.joystick;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class JoyStickView extends FrameLayout implements View.OnTouchListener {
    private int inset = 75;

    private final Object LOCK = new Object();

    public JoyStickView(Context context) {
        super(context);
    }

    public JoyStickView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public JoyStickView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public JoyStickView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        View view = LayoutInflater.from(getContext()).inflate(R.layout.controller, this, false);
        addView(view);

        findViewById(R.id.joyStick_action_view).setOnTouchListener(this);

        timer.schedule(timerTask,16,16);

    }

    Timer timer = new Timer();

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            synchronized (LOCK) {
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        callOnMove(xValue, yValue);
                    }
                });
            }
        }
    };

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        timer.cancel();
        timer.purge();
    }

    private List<OnControllerListener> listeners = new ArrayList<>();

    public void addOnControllerListener(OnControllerListener listener) {
        listeners.add(listener);
    }

    public void removeOnControllerListener(OnControllerListener listener) {
        listeners.remove(listener);
    }


    private void callOnMove(double x, double y) {
        for (OnControllerListener l : listeners) {
            l.onMove(x, y);
        }
    }


    private double lastX;
    private double lastY;

    private boolean isCoordinateChanged(double xCord, double yCord) {
        boolean isChanged = xCord != lastX && yCord != lastY;
        lastX = xCord;
        lastY = yCord;
        return isChanged;
    }

    private int _xDelta;
    private int _yDelta;

    private double xValue;
    private double yValue;

    @Override
    public boolean onTouch(final View view, MotionEvent motionEvent) {
        final int X = (int) motionEvent.getRawX();
        final int Y = (int) motionEvent.getRawY();
        switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                _xDelta = X - lParams.leftMargin;
                _yDelta = Y - lParams.topMargin;
                return true;
            case MotionEvent.ACTION_UP:
                xValue = 0;
                yValue = 0;
                _xDelta = 0;
                _yDelta = 0;
                view.animate().translationX(0)
                        .translationY(0).setDuration(300)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                invalidate();
                            }
                        }).start();
                return false;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                float r = getWidth() / 2f - inset;

                float x = X - _xDelta;
                float y = Y - _yDelta;


                // do translate outside of the circle.
                if (x * x + y * y > r * r) {
                    int ix = x < 0 ? -1 : 1;
                    int iy = y < 0 ? -1 : 1;
                    if (x < 0) x *= -1;
                    if (y < 0) y *= -1;
                    x = (float) Math.sqrt((r * r) / (1 + ((y * y) / (x * x))));
                    y = (float) Math.sqrt(r * r - x * x);

                    x *= ix;
                    y *= iy;
                }

                xValue = (x * 2) / (getWidth() - inset * 2);
                yValue = (y * 2) / (getWidth() - inset * 2);

                view.animate().translationX(x).translationY(y).setDuration(0).start();

                invalidate();
                return true;
        }

        return false;
    }

    public interface OnControllerListener {
        void onMove(double xInterval, double yInterval);
    }
}
