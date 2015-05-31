package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.widget.ImageButton;

/**
 * Created by Matias Grioni on 12/29/14.
 */
public class FloatingActionButton extends ImageButton {
    private int bgcolor;
    private int bgcolorPressed;

    /**
     *
     * @param context
     */
    public FloatingActionButton(Context context) {
        super(context);
        init(null);
    }

    /**
     *
     * @param context
     * @param attrs
     */
    public FloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    /**
     *
     * @param context
     * @param attrs
     * @param defStyle
     */
    public FloatingActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     *
     * @param attrs
     */
    private void init(AttributeSet attrs) {
        Resources.Theme theme = getContext().getTheme();
        TypedArray arr = theme.obtainStyledAttributes(attrs, R.styleable.FloatingActionButton, 0, 0);

        try {
            setBgcolor(arr.getColor(R.styleable.FloatingActionButton_bgcolor, Color.BLUE));
            setBgcolorPressed(arr.getColor(R.styleable.FloatingActionButton_bgcolor_pressed, Color.GRAY));

            StateListDrawable stateList = new StateListDrawable();
            stateList.addState(new int[] { android.R.attr.state_pressed }, createButton(bgcolorPressed));
            stateList.addState(new int[] { }, createButton(bgcolor));
            setBackground(stateList);
        } finally {
            arr.recycle();
        }
    }

    private Drawable createButton(int color) {
        OvalShape oval = new OvalShape();
        ShapeDrawable shape = new ShapeDrawable(oval);
        setWillNotDraw(false);
        shape.getPaint().setColor(color);

        return shape;
    }

    /**
     *
     * @param color
     */
    public void setBgcolor(int color) {
        bgcolor = color;
    }

    /**
     *
     * @param color
     */
    public void setBgcolorPressed(int color) {
        bgcolorPressed = color;
    }
}
