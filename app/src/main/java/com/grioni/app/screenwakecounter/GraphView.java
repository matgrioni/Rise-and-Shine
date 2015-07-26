package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matias Grioni on 12/27/14.
 */
public class GraphView extends View {
    private static float AXIS_PADDING = 5;
    private static float TOP_GRAPH_PADDING = 7;

    private static long REARRANGE_GRAPH_ANIM_LENGTH = 250;
    private static long POINT_ADD_ANIM_LENGTH = 250;
    private long animStartTime = 0;
    private int nextPoint;

    private int pixelAxisPadding;
    private int topPixelGraphPadding;

    private String xaxis;
    private List<Integer> points;

    private int textSize;
    private int graphStartX;
    private int graphStartY;

    private int graphHeight;
    private int graphWidth;

    private double verticalUnitToPx;
    private int horizontalUnitToPx;

    private int lineColor;
    private int shadeColor;

    /**
     *
     * @param context
     */
    public GraphView(Context context) {
        super(context);
        init(null);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray arr = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.GraphView, 0, 0);
        final float scale = getResources().getDisplayMetrics().density;

        pixelAxisPadding = (int) (AXIS_PADDING * scale);
        topPixelGraphPadding = (int) (TOP_GRAPH_PADDING * scale);

        try {
            float unscaledText = arr.getDimension(R.styleable.GraphView_textSize, 12);
            textSize = (int) (unscaledText * scale + 0.5f);

            lineColor = arr.getColor(R.styleable.GraphView_lineColor, Color.BLUE);
            shadeColor = arr.getColor(R.styleable.GraphView_shadeColor, Color.GRAY);
        } finally {
            arr.recycle();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        drawGraphLabels(canvas);
        drawAxes(canvas);

        if(points != null) {
            if(points.size() != 0) {
                drawData(canvas);
                drawShading(canvas);
            }
        }
    }

    private void drawGraphLabels(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(textSize);

        // Draw the axes for the graph including labels
        Rect xaxisBounds = new Rect();
        paint.getTextBounds(xaxis, 0, xaxis.length(), xaxisBounds);
        canvas.drawText(xaxis, getWidth() / 2 - xaxisBounds.width() / 2,
                getHeight() - xaxisBounds.bottom, paint);

        Rect yaxisBounds = new Rect();
        paint.getTextBounds("Views", 0, 5, yaxisBounds);

        canvas.save();
        canvas.rotate(-90.f, yaxisBounds.height(), getHeight() / 2 + yaxisBounds.width() / 2);
        canvas.drawText("Views", yaxisBounds.height(), getHeight() / 2 + yaxisBounds.width() / 2, paint);
        canvas.restore();

        graphStartX = yaxisBounds.height() + pixelAxisPadding;
        graphStartY = getHeight() - xaxisBounds.height() - pixelAxisPadding;

        graphHeight = graphStartY - topPixelGraphPadding;
        graphWidth = getWidth() - graphStartX;
    }

    /**
     *
     * @param canvas
     */
    private void drawAxes(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(2.5f);
        paint.setColor(Color.GRAY);

        canvas.drawLine(graphStartX, graphStartY, getWidth(), graphStartY, paint);
        canvas.drawLine(graphStartX, graphStartY, graphStartX, 0, paint);
    }

    /**
     *
     * @param canvas
     */
    private void drawShading(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(0);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(shadeColor);

        Path path = new Path();
        path.moveTo(graphStartX, graphStartY);

        for(int i = 0; i < points.size(); i++)
            path.lineTo(graphStartX + horizontalUnitToPx * i,
                    graphStartY - (int) (points.get(i) * verticalUnitToPx));
        path.lineTo(graphStartX + horizontalUnitToPx * (points.size() - 1), graphStartY);
        path.close();

        canvas.drawPath(path, paint);
    }

    /**
     *
     * @param canvas
     */
    private void drawData(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(lineColor);
        paint.setStrokeWidth(2.5f);

        // If the max is larger than the graph height, then the result of division will be zero
        // and the graph will flatline.
        int max = max(points);
        verticalUnitToPx = (max == 0 || graphHeight < max) ?
                (double) max / graphHeight : (double) graphHeight / max;

        if(points.size() > 1) {
            horizontalUnitToPx = graphWidth / (points.size() - 1);

            int priorX = graphStartX;
            int priorY = (int) (points.get(0) * verticalUnitToPx);
            int currentX = graphStartX - 1;

            for (int i = 1; i < points.size(); i++) {
                currentX += horizontalUnitToPx;
                if (i % points.size() == 0)
                    currentX++;

                int currentY = (int) (points.get(i) * verticalUnitToPx);
                canvas.drawLine(priorX, graphStartY - priorY, currentX, graphStartY - currentY, paint);
                priorX = currentX;
                priorY = currentY;
            }
        } else if(points.size() == 1) {
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(graphStartX, graphHeight * .4f, 6, paint);
        }
    }

    /**
     *
     */
    private void adjustUnits() {
        float interpolatedTime = (SystemClock.elapsedRealtime() - animStartTime) / POINT_ADD_ANIM_LENGTH;

        int endHorizontalUnitToPx = graphWidth / points.size();
        int startHorizontalUnitToPx = graphWidth / (points.size() - 1);
        horizontalUnitToPx = startHorizontalUnitToPx - (int) ((startHorizontalUnitToPx - endHorizontalUnitToPx)
                * (interpolatedTime));

        int nextMax = max(points);
        if(nextPoint > nextMax)
            nextMax = nextPoint;
        int endVerticalUnitToPx = (nextMax == 0) ? 0 : graphHeight / nextMax;
        int startVerticalUnitToPx = (max(points) == 0) ? 0 : graphHeight / max(points);
        verticalUnitToPx = startVerticalUnitToPx - (int) ((startVerticalUnitToPx - endVerticalUnitToPx)
                * (interpolatedTime));
     }

    private int max(List<Integer> points) {
        int max = points.get(0);
        for(int i = 1; i < points.size(); i++)
            if(points.get(i) > max)
                max = points.get(i);

        return max;
    }


    /**
     *
     * @param xaxis
     */
    public void setAxis(String xaxis) {
        if(xaxis == null)
            xaxis = "";
        this.xaxis = xaxis;
    }

    /**
     *
     * @param points
     */
    public void setData(List<Integer> points) {
        if(points == null)
            points = new ArrayList<Integer>();
        this.points = points;
    }

    /**
     *
     * @param point
     */
    public void addPoint(int point) {
        nextPoint = point;
        animStartTime = SystemClock.elapsedRealtime();
    }
}
