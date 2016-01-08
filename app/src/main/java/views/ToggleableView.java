package views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.grioni.app.screenwakecounter.R;

/**
 * @author Matias Grioni
 * @created 1/5/16
 */
public class ToggleableView extends LinearLayout {

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

    private OnToggleListener toggleListener;

    private ToggleViewAnimation toggle;
    private boolean isCollapsed;

    private boolean marginInit;
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

        if (isCollapsed) {
            ((RelativeLayout.LayoutParams) getLayoutParams()).bottomMargin = -1 * getHeight();
        }
    }

    /**
     *
     */
    private void init() {
        isCollapsed = false;
        marginInit = false;
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
     */
    private void setupAnimation() {
        if (!marginInit) {
            originalMargin = ((RelativeLayout.LayoutParams) getLayoutParams()).bottomMargin;
            marginInit = true;
        }

        toggle = new ToggleViewAnimation(this, 500, 0);
        toggle.setAnimationListener(animListener);
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
     * @return
     */
    public void show() {
        if (isCollapsed) {
            setupAnimation();
            this.startAnimation(toggle);

            isCollapsed = !isCollapsed;
        }
    }

    public void hide() {
        if (!isCollapsed) {
            setupAnimation();
            this.startAnimation(toggle);

            isCollapsed = !isCollapsed;
        }
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
}
