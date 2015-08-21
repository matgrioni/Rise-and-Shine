package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * @author - Matias Grioni
 * @created - 1/25/15.
 *
 * The bottom border that follows the padding of the RecyclerView items. The
 * border will not extend through the padding, it will stop short of the left
 * and right padding.
 */
public class TimeCardItemBottomBorder extends RecyclerView.ItemDecoration {
    private Drawable divider;

    /**
     * Create the ItemDecorator.
     *
     * @param context - Context to load the image resource for the border.
     */
    public TimeCardItemBottomBorder(Context context) {
        divider = context.getResources().getDrawable(R.drawable.card_divider);
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        // Get the left and right edge of the non-padded view so that the
        // divider ends before the screen edges.
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        // For each child (entry in the RecyclerView) draw the divider at the
        // bottom of the view.
        int childCount = parent.getChildCount();
        for(int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            divider.setBounds(left, top, right, bottom);
            divider.draw(canvas);
        }
    }
}
