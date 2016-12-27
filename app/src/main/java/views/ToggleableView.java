package views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.grioni.app.screenwakecounter.R;

/**
 * @author Matias Grioni
 * @created 1/5/16
 */
public class ToggleableView extends LinearLayout {

    private class ToggleAnimation extends Animation {
        private boolean animEnded;

        private View animatedView;
        private RelativeLayout.LayoutParams layoutParams;

        // The margin that the view starts and the margin it should end
        // at when the animation finishes.
        private int marginStart;
        private int marginEnd;

        /**
         * Create the animation to collapse and expand the TimeCard.
         *
         * @param view     - The view to animate.
         * @param duration - The duration of the animation.
         * @param marginStart
         * @param marginEnd
         */
        public ToggleAnimation(View view, int duration, int marginStart, int marginEnd) {
            animEnded = false;
            setDuration(duration);

            animatedView = view;
            layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();

            this.marginStart = marginStart;
            this.marginEnd = marginEnd;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);

            // If the animation is still ongoing then move the margins as necessary.
            // Once the animation has ended ensure the interpolatedTime did the
            // right thing and got the margin to the end.
            if (interpolatedTime < 1.0f) {
                layoutParams.bottomMargin = marginStart + (int) ((marginEnd - marginStart) * interpolatedTime);
                animatedView.requestLayout();
            } else if (!animEnded) {
                layoutParams.bottomMargin = marginEnd;
                animatedView.requestLayout();

                animEnded = true;
            }
        }
    }

    private Animation.AnimationListener animListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (toggleListener != null)
                toggleListener.onToggle(ToggleableView.this, isCollapsed);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    /**
     *
     */
    public interface OnToggleListener {
        void onToggle(View v, boolean isCollapsed);
    }

    private boolean drawInit;

    private OnToggleListener toggleListener;

    private ToggleAnimation toggle;
    private boolean isCollapsed;

    private int originalHeight;
    private int originalMargin;

    public ToggleableView(Context context) {
        super(context);
        init();
    }

    public ToggleableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        setupAttrs(attrs);
    }

    public ToggleableView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        setupAttrs(attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if (isCollapsed && !drawInit) {
            originalMargin = getBottomMargin();
            originalHeight = getHeight();

            ((RelativeLayout.LayoutParams) getLayoutParams()).bottomMargin = -1 * getHeight();
            requestLayout();
            invalidate();
        }

        drawInit = true;
    }

    /**
     *
     */
    private void init() {
        isCollapsed = false;
        /*getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ((RelativeLayout.LayoutParams) getLayoutParams()).bottomMargin = -1 * getHeight();
                invalidate();
            }
        });*/
    }

    /**
     *
     * @param attrs
     */
    private void setupAttrs(AttributeSet attrs) {
        TypedArray arr = getContext().obtainStyledAttributes(attrs, R.styleable.ToggleableView, 0, 0);

        try {
            isCollapsed = arr.getBoolean(R.styleable.ToggleableView_collapsed, false);
        } finally {
            arr.recycle();
        }
    }

    /**
     *
     * @param collapsed
     */
    public void setCollapsed(boolean collapsed) {
        isCollapsed = collapsed;

        if (isCollapsed)
            hide();
        else
            show();
    }

    /**
     *
     * @return
     */
    public boolean toggle() {
        if (isCollapsed)
            show();
        else
            hide();

        return isCollapsed;
    }

    /**
     *
     */
    public void show() {
        if (drawInit && isCollapsed) {
            toggle = new ToggleAnimation(this, 500, getBottomMargin(), originalMargin);
            toggle.setAnimationListener(animListener);

            this.startAnimation(toggle);
        }

        isCollapsed = false;
    }

    /**
     *
     */
    public void hide() {
        if (drawInit && !isCollapsed) {
            toggle = new ToggleAnimation(this, 500, getBottomMargin(), -getHeight());
            toggle.setAnimationListener(animListener);

            this.startAnimation(toggle);
        }

        isCollapsed = true;
    }

    /**
     *
     * @param onToggleListener
     */
    public void setOnToggleListener(OnToggleListener onToggleListener) {
        this.toggleListener = onToggleListener;
    }

    /**
     *
     * @return
     */
    public boolean isCollapsed() {
        return isCollapsed;
    }

    private int getBottomMargin() {
        return ((RelativeLayout.LayoutParams) getLayoutParams()).bottomMargin;
    }
}
