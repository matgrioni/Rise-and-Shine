package com.grioni.app.screenwakecounter;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by Matias Grioni on 1/13/15.
 */
public class GraphAnimation extends Animation {
    private boolean alreadyEnded;

    private View animatedView;
    private RelativeLayout.LayoutParams layoutParams;
    private int marginStart;
    private int marginEnd;

    /**
     *
     * @param view
     * @param duration
     */
    public GraphAnimation(View view, int duration) {
        setDuration(duration);

        animatedView = view;
        layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

        marginStart = layoutParams.bottomMargin;
        marginEnd = (marginStart == 0) ? (-1 * view.getHeight()) : 0;
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        if(interpolatedTime < 1.0f) {
            layoutParams.bottomMargin = marginStart + (int) ((marginEnd - marginStart) * interpolatedTime);
            animatedView.requestLayout();
        } else if(!alreadyEnded) {
            layoutParams.bottomMargin = marginEnd;
            animatedView.requestLayout();

            alreadyEnded = true;
        }
    }
}
