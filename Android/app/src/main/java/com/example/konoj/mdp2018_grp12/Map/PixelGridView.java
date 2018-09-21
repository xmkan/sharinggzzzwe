package com.example.konoj.mdp2018_grp12.Map;


import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.konoj.mdp2018_grp12.BluetoothService.BluetoothFrag;
import com.example.konoj.mdp2018_grp12.MainActivity;
import com.example.konoj.mdp2018_grp12.R;

import java.math.BigInteger;
import java.util.Locale;

public class PixelGridView extends android.support.v7.widget.AppCompatImageView
{
    private static int numColumns, numRows;
    private static int cellWidth, cellHeight;
    private Paint whitePaint = new Paint();
    private Paint blackPaint = new Paint();
    private Paint bluePaint = new Paint();
    private Paint redPaint = new Paint();
    private Paint greenPaint = new Paint();
    private Paint wpPaint = new Paint();
    private Paint bodyPaint=new Paint();
    private Paint headPaint = new Paint();
    private boolean[][] cellChecked;
    private static MapDecoder md = new MapDecoder();
    private float scale = 1f;
    private ScaleGestureDetector SGD;



    public PixelGridView(Context context)
    {
        this(context, null);
    }

    public PixelGridView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        //blackPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        whitePaint.setColor(Color.WHITE);
        blackPaint.setColor(Color.BLACK);
        bluePaint.setColor(Color.BLUE);
        redPaint.setColor(Color.RED);
        greenPaint.setColor(Color.GREEN);
        headPaint.setColor(Color.MAGENTA);
        wpPaint.setColor(Color.GRAY);
        bodyPaint.setColor(Color.rgb(219,159,68));
    }

    public void wpShow(){
        md.showWp();
    }

    public void wpHide(){md.hideWp();}

    public void setNumColumns(int numColumns)
    {
        this.numColumns = numColumns;
        calculateDimensions();
    }

    public int getNumColumns()
    {
        return numColumns;
    }

    public void setNumRows(int numRows)
    {
        this.numRows = numRows;
        calculateDimensions();
    }

    public int getNumRows()
    {
        return numRows;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions();
    }

    private void calculateDimensions()
    {
        if (numColumns == 0 || numRows == 0)
            return;

        cellWidth = getWidth() / numColumns;
        cellHeight = getHeight() / numRows;


        cellChecked = new boolean[numColumns][numRows];

        invalidate();
    }



    public void setWaypoint(int x,int y){
        md.updateWaypoint(y,x);
    }


    public void updateDemoArenaMap(String obstacleMapDes){
        md.updateDemoMapArray(obstacleMapDes);
    }

    public void updateDemoRobotPos(String robotPos){
        md.updateDemoRobotPos(robotPos);
    }

    public void updateRobotPos(String robotPos){
        md.updateRobotPos(robotPos);
    }

    public void updateArena(String obstacleMap, String objectMap){
        md.updateMapArray(obstacleMap,objectMap);
    }

    public void clearMap(){
        md.clearMapArray();
    }

    @Override
    protected void onDraw(Canvas canvas)
    {

        super.onDraw(canvas);
        canvas.scale(scale, scale);

        int[][] testMap = md.decodeMapDescriptor();



        //canvas.drawColor(Color.BLACK);

        if (numColumns == 0 || numRows == 0)
            return;

        int width = getWidth()-5;
        int height = getHeight();

        for (int i = 0; i < numColumns; i++)
        {
            for (int j = 0; j < numRows ; j++)
            {
                if (testMap[j][i] == 0)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, whitePaint);
                }
                if (testMap[j][i] == 1)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, greenPaint);
                }
                if (testMap[j][i] == 2)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, redPaint);
                }
                if (testMap[j][i] == 3)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, bodyPaint);
                }
                if (testMap[j][i] == 4)
                {
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, headPaint);
                }
                if(testMap[j][i] == 5){
                   /* Drawable d = getResources().getDrawable(R.drawable.waypoint_icon);
                    d.setBounds(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight);
                    d.draw(canvas);*/
                    canvas.drawRect(i * cellWidth, j * cellHeight, (i + 1) * cellWidth, (j + 1) * cellHeight, wpPaint);
                }
            }
        }

        for (int i = 1; i < numColumns; i++)
        {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, blackPaint);
        }

        for (int i = 1; i < numRows; i++)
        {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, blackPaint);
        }




    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (event.getAction() != MotionEvent.ACTION_DOWN)
            return true;

        int column = (int)(event.getX() / cellWidth);
        int row = (int)(event.getY() / cellHeight);

        Log.e("Touch Coordinate",column+", "+row);

       BluetoothFrag.setCoor(column,row);


        //cellChecked[column][row] = !cellChecked[column][row];
        invalidate();

        return true;
    }


}