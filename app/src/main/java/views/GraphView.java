package views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.grioni.app.screenwakecounter.R;

import java.util.ArrayList;
import java.util.List;

import utils.DataUtils;

/**
 * @author - Matias Grioni
 * @created - 12/27/14
 *
 * Displays a Graph or plot of the provided points. The colors of the line and area beneath it can
 * be set in the xml resources.
 */
public class GraphView extends View {
    // The unscaled margin between the axis line and the edge of the view and the margin between the
    // top edge and the top of the graph.
    private static final float DEFAULT_UNSCALED_AXIS_MARGIN = 5;
    private static final float DEFAULT_UNSCALED_AXIS_TEXT_SIZE = 12;

    // The axis margin is the distance between the axis labels and the actual
    // axis line.
    private int axisMargin;
    private String xAxisLabel;
    private String yAxisLabel;

    private List<Integer> points;
    private List<Integer> selected;

    // This is the x position of the x-axis and the y position of the y-axis
    // respectively.
    private int graphStartX;
    private int graphStartY;

    private int graphHeight;
    private int graphWidth;

    private double verticalUnitToPx;
    private int horizontalUnitToPx;

    private int textSize;
    private int lineColor;
    private int shadeColor;

    /**
     * Create the GraphView given the context.
     *
     * @param context - Context to create the GraphView with.
     */
    public GraphView(Context context) {
        super(context);
        init();
        initAttrs(null);
    }

    /**
     * Create the GraphView given the context and xml attributes.
     *
     * @param context - Context to create the GraphView with.
     * @param attrs - XML attributes for the GraphView.
     */
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttrs(attrs);
    }

    /**
     * Create the GraphView given the context, xml attributes, and default style.
     *
     * @param context - Context to create the GraphView with.
     * @param attrs - XML attributes for the GraphView.
     * @param defStyle - The default style resource for the view.
     */
    public GraphView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        initAttrs(attrs);
    }

    private void init() {
        points = new ArrayList<>();
        selected = new ArrayList<>();
    }

    /**
     * Initialize the attributes for this GraphView based on the xml attributes provided.
     *
     * @param attrs - The attribute set to use to get the xml definition.
     */
    private void initAttrs(AttributeSet attrs) {
        TypedArray arr = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.GraphView, 0, 0);

        try {
            float scale = getResources().getDisplayMetrics().density;
            textSize = (int) arr.getDimension(R.styleable.GraphView_textSize,
                    DEFAULT_UNSCALED_AXIS_TEXT_SIZE * scale);

            setXAxis(getDefaultableStringResource(arr, R.styleable.GraphView_xaxis, ""));
            setYAxis(getDefaultableStringResource(arr, R.styleable.GraphView_yaxis, ""));

            axisMargin = (int) arr.getDimension(R.styleable.GraphView_axisMargin,
                                                DEFAULT_UNSCALED_AXIS_MARGIN * scale);

            lineColor = arr.getColor(R.styleable.GraphView_lineColor, Color.BLACK);
            shadeColor = arr.getColor(R.styleable.GraphView_shadeColor, Color.WHITE);
        } finally {
            arr.recycle();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        drawAxisLabels(canvas);
        drawAxes(canvas);

        if (!empty()) {
            drawData(canvas);
            drawShading(canvas);
            drawSelected(canvas);
        }
    }

    /**
     *
     * @param canvas
     */
    private void drawAxisLabels(Canvas canvas) {
        // Create a paint object that is anti-aliased and has the desired text size.
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(textSize);

        Rect xAxisBounds = new Rect();
        paint.getTextBounds(xAxisLabel, 0, xAxisLabel.length(), xAxisBounds);

        Rect yAxisBounds = new Rect();
        paint.getTextBounds(yAxisLabel, 0, yAxisLabel.length(), yAxisBounds);

        graphStartX = yAxisBounds.height() + axisMargin + getPaddingStart();
        graphStartY = getHeight() - xAxisBounds.height() - axisMargin - getPaddingBottom();

        graphWidth = getWidth() - graphStartX - getPaddingEnd();
        graphHeight = graphStartY - getPaddingTop();

        // NOTE: For drawing text, the bounded rectangle around the text is not with origin in the
        // top left corner as might be assumed. The x-axis varies within the text depending on the
        // letters. For this reason to calculate where to draw text use the top, bottom, right, and
        // left values of the rectangles measuring the text size.
        canvas.drawText(xAxisLabel, graphStartX + graphWidth / 2 - xAxisBounds.width() / 2,
                graphStartY + axisMargin - xAxisBounds.top, paint);

        canvas.save();
        canvas.rotate(-90.f, graphStartX - axisMargin, graphHeight / 2 + yAxisBounds.width() / 2);
        canvas.drawText(yAxisLabel, graphStartX - axisMargin, graphHeight / 2 + yAxisBounds.width() / 2, paint);
        canvas.restore();
    }

    /**
     *
     * @param canvas
     */
    private void drawAxes(Canvas canvas) {
        Paint paint = new Paint();
        paint.setStrokeWidth(2.5f);
        paint.setColor(Color.GRAY);

        canvas.drawLine(graphStartX, graphStartY, getWidth() - getPaddingEnd(), graphStartY, paint);
        canvas.drawLine(graphStartX, graphStartY, graphStartX, getPaddingTop(), paint);
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
        int max = DataUtils.max(points);
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

    private void drawSelected(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(lineColor);
        paint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < selected.size(); i++) {
            int pointIndex = selected.get(i);
            int selectedX = graphStartX + pointIndex * horizontalUnitToPx;
            int selectedY = (int) (points.get(pointIndex) * verticalUnitToPx);
            canvas.drawCircle(selectedX, graphStartY - selectedY, 8, paint);
        }
    }

    /**
     *
     * @param xAxis
     */
    public void setXAxis(String xAxis) {
        this.xAxisLabel = xAxis;
    }

    public void setYAxis(String yAxis){
        this.yAxisLabel = yAxis;
    }

    /**
     *
     * @param points
     */
    public void setData(List<Integer> points) {
        if(points == null)
            points = new ArrayList<>();
        this.points = points;
    }

    /**
     * Select the given point in the graph.
     *
     * @param pointIndex The index of the point on in the data list.
     */
    public void addSelected(int pointIndex) {
        selected.add(pointIndex);
        invalidate();
    }

    /**
     * Deselect the given point in the graph.
     *
     * @param pointIndex The index of the point in the data list.
     * @return True if the point was deselected and false otherwise.
     */
    public boolean removeSelected(int pointIndex) {
        boolean result = selected.remove(Integer.valueOf(pointIndex));
        invalidate();
        return result;
    }

    /**
     * Clear all selected points to non-selected.
     */
    public void clearSelected() {
        selected.clear();
        invalidate();
    }

    /**
     * Check if this GraphView is representing any data.
     *
     * @return True if there there was no data set to display. False otherwise.
     */
    public boolean empty() {
        return this.points.size() == 0;
    }

    /**
     *
     * @param arr
     * @param id
     * @param defValue
     * @return
     */
    private String getDefaultableStringResource(TypedArray arr, int id, String defValue){
        String res = arr.getString(id);
        if (res == null)
            res = defValue;

        return res;
    }
}
