package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import models.TimeCard;
import models.TimeCardCache;
import models.TimeInterval;
import views.GraphView;

/**
 * Created by Matias Grioni on 1/15/15.
 */
public class TimeCardHolder extends RecyclerView.ViewHolder {
    private Animation.AnimationListener graphAnimListener = new Animation.AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            cardEventListener.onCardStateChanged(getPosition());
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    };

    private View.OnClickListener actionClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            View cardView = (View) v.getParent();
            GraphView graph = (GraphView) cardView.findViewById(R.id.graph);

            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) graph.getLayoutParams();

            if (layoutParams.bottomMargin == 0)
                ((ImageView) v).setImageResource(R.drawable.card_expand);
            else if(layoutParams.bottomMargin < 0)
                ((ImageView) v).setImageResource(R.drawable.card_collapse);

            GraphCollapseAnimation animation = new GraphCollapseAnimation(graph, 500);
            animation.setAnimationListener(graphAnimListener);
            graph.startAnimation(animation);
        }
    };

    private TimeCardEventListener cardEventListener;

    public TextView title;
    public GraphView graph;

    public ImageView action;
    public ImageView options;

    public PopupMenu popupMenu;

    private Context baseContext;

    private TimeCardCache cache;

    /**
     *
     * @param parent
     */
    public TimeCardHolder(View parent) {
        super(parent);

        title = (TextView) parent.findViewById(R.id.interval_count);
        graph = (GraphView) parent.findViewById(R.id.graph);

        action = (ImageView) parent.findViewById(R.id.card_action);
        options = (ImageView) parent.findViewById(R.id.card_options);

        popupMenu = new PopupMenu(parent.getContext(), options);
        popupMenu.inflate(R.menu.card_options);

        baseContext = parent.getContext();
    }

    public void setCache(TimeCardCache cache) {
        this.cache = cache;
    }

    /**
     *
     */
    private void setListeners(final TimeCard card) {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardEventListener.onCardClicked(getPosition());
            }
        });

        if(card.collapsed)
            action.setImageResource(R.drawable.card_expand);
        else
            action.setImageResource(R.drawable.card_collapse);

        action.setOnClickListener(actionClicked);
        options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupMenu.show();
            }
        });

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.card_share:
                        String shareText = "Screen was turned on " + cache.count + " times in the last ";
                        if(card.backCount == 1)
                            shareText += card.interval.name().toLowerCase();
                        else
                            shareText += card.backCount + " " + card.interval.name().toLowerCase() + "s";

                        Intent shareIntent = new Intent();
                        shareIntent.setAction(Intent.ACTION_SEND);
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                        shareIntent.setType("text/plain");
                        baseContext.startActivity(Intent.createChooser(shareIntent,
                                baseContext.getResources().getString(R.string.share_count)));

                        return true;
                }

                return false;
            }
        });
    }

    /**
     *
     */
    public void bindHolder(TimeCard card) {
        String label = "Last ";
        if (card.backCount > 1)
            label += Integer.toString(card.backCount) + " " + card.interval.name() + "s";
        else
            label += card.interval.name();
        title.setText(label + ": " + cache.count);

        String axis = "Hour";
        if (card.backCount == 1) {
            if (card.interval != TimeInterval.Day)
                axis = "Day";
        } else
            axis = card.interval.name();
        graph.setAxis(axis);
        graph.setData(cache.data);

        if(!card.collapsed) {
            ViewGroup.MarginLayoutParams params =
                    (ViewGroup.MarginLayoutParams) graph.getLayoutParams();
            params.bottomMargin = 0;
            graph.setLayoutParams(params);
        }

        setListeners(card);
    }

    /**
     *
     * @param cardEventListener
     */
    public void setCardEventListener(TimeCardEventListener cardEventListener) {
        this.cardEventListener = cardEventListener;
    }
}
