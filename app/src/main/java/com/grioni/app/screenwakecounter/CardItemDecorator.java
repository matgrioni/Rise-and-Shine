package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by Matias Grioni on 1/25/15.
 */
public class CardItemDecorator extends RecyclerView.ItemDecoration {
    private Drawable divider;

    /**
     *
     * @param context
     */
    public CardItemDecorator(Context context) {
        divider = context.getResources().getDrawable(R.drawable.card_divider);
    }

    /**
     *
     * @param canvas
     * @param parent
     * @param state
     */
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

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
