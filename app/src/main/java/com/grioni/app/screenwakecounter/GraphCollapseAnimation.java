package com.grioni.app.screenwakecounter;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.RelativeLayout;

/**
 * @author - Matias Grioni
 * @created - 1/13/15
 *
 * The animation to collapse and expand the TimeCard.
 */
public class GraphCollapseAnimation extends Animation {
    private boolean alreadyEnded;

    private View animatedView;
    private RelativeLayout.LayoutParams layoutParams;

    // The margin that the view starts and the margin it should end
    // at when the animation finishes.
    private int marginStart;
    private int marginEnd;

    /**
     * Create the animation to collapse and expand the TimeCard.
     *
     * @param view - The view to animate.
     * @param duration - The duration of the animation.
     */
    public GraphCollapseAnimation(View view, int duration) {
        setDuration(duration);

        animatedView = view;
        layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

        // The margin starts at whatever the view is currently at, then would
        // end at 0 if the margin is already negative. If the margin is at
        // least positive or 0 then we end the margin animation once the entire
        // height is covered by the margin.
        marginStart = layoutParams.bottomMargin;
        marginEnd = (marginStart < 0) ? 0 : (-1 * view.getHeight());
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        super.applyTransformation(interpolatedTime, t);

        // If the animation is still ongoing then move the margins as necessary.
        // Once the animation has ended ensure the interpolatedTime did the
        // right thing and got the margin to the end.
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
