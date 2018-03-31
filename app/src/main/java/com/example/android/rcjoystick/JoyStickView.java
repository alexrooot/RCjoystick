package com.example.android.rcjoystick;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Alex on 3/25/2018.
 */

public class JoyStickView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    String address = null, name = null;
    BluetoothAdapter myBluthoot = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevice;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    int ydataInt =0;
    int xdataInt = 0;
    float centerX;
    float centerY;
    float baseRadius;
    float hatRadius;
    private JoystickListener joystickCallb;

    void setupDimensions(){
        centerX = getWidth()/2;
        centerY = getHeight()/2;
        baseRadius = Math.min(getWidth(),getHeight())/3;
        hatRadius = Math.min(getWidth(),getHeight())/5;
    }

    public JoyStickView(Context context){
        super (context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallb = (JoystickListener) context;
    }

    public JoyStickView (Context context, AttributeSet atttributes, int style){
        super(context,atttributes,style);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallb = (JoystickListener) context;
    }

    public JoyStickView (Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener)
            joystickCallb = (JoystickListener) context;
    }


    private void drawJoystick(float newX, float newY){
        if (getHolder().getSurface().isValid()) {
            //build a view type of canvas
            Canvas myCanvas = this.getHolder().lockCanvas();
            // setup a variable for color
            Paint colorsBaseRadius = new Paint();
            Paint colorRadiusHat = new Paint();
            //give the canvas a color of transparent
            myCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            //give variable colors and actual color
            colorsBaseRadius.setARGB(150, 45, 45, 200);
            colorRadiusHat.setARGB(235, 45, 45, 200);
            //call canvas view and give it data baseRadius of draw circle with X,Y coordinates
            myCanvas.drawCircle(centerX, centerY, baseRadius, colorsBaseRadius);
            myCanvas.drawCircle(newX, newY, hatRadius, colorRadiusHat);

            getHolder().unlockCanvasAndPost(myCanvas);

        }
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        setupDimensions();
        drawJoystick(centerX,centerY);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public boolean onTouch (View v, MotionEvent e){
        // if the canvas view is setup = true continue on
        if (v.equals(this)){
            //Find the displacement value in order to stay inside the baseline circle
            //used her as this is when the program checks if the user is touching inside the canvas
            float displacement = (float) Math.sqrt(Math.pow(e.getX()- centerX,2)+Math.pow(e.getY() - centerY, 2));
            // If the finger is touching the joystick
            if(e.getAction() != e.ACTION_UP){
                //if the hat does not go out side the baseline just draw it
                if (displacement < baseRadius){

                    // this getX/Y gets the coordinates based on pixel shift
                    drawJoystick(e.getX(),e.getY());

                    //converts the the hat and base difference into a percentage
                    joystickCallb.onJoystickMoved((e.getX() - centerX) / baseRadius, (e.getY() - centerY) / baseRadius, getId());
                    xdataInt = (int) (e.getX() - centerX / baseRadius);
                    ydataInt = (int) (e.getY() - centerY / baseRadius);


                }else{
                    //else make the hat be restricted
                    float ration = baseRadius / displacement;
                    float constrainedX = centerX + (e.getX()- centerX)* ration;
                    float constrainedY = centerY + (e.getX()- centerY)* ration;
                    //converts the the hat and base difference into a percentage
                    joystickCallb.onJoystickMoved((constrainedX - centerX) / baseRadius, (constrainedY - centerY) / baseRadius, getId());
                    xdataInt = (int) (e.getX() - centerX / baseRadius);
                    ydataInt = (int) (e.getY() - centerY / baseRadius);

                }

            }
            else {
                // else if finger is not touching set joy stick to be center
                drawJoystick(centerX,centerY);
                //when the finger get pulled up the callback set the percentage to 0 ,0 %
                joystickCallb.onJoystickMoved(0,0,getId());

            }
        }
        return true;
    }





    public interface JoystickListener{

        void onJoystickMoved(float xPercent, float yPercent, int source);

    }


}
