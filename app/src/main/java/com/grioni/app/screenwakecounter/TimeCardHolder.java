package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import models.TimeCard;
import models.TimeCardCache;
import models.TimeInterval;
import utils.LabelUtils;
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
            cardEventListener.onCardStateChanged(card.id);
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

    private Context baseContext;
    private TimeCardEventListener cardEventListener;

    public TextView title;
    public GraphView graph;

    public ImageView action;
    public ImageView share;

    private TimeCard card;
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
        share = (ImageView) parent.findViewById(R.id.card_options);

        baseContext = parent.getContext();
    }

    public void setCache(TimeCardCache cache) {
        this.cache = cache;
    }

    /**
     *
     */
    private void setListeners() {
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardEventListener.onCardClicked(card.id);
            }
        });

        action.setOnClickListener(actionClicked);

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String shareText = "Screen was turned on " + cache.count + " times in the last ";
                if (card.backCount == 1)
                    shareText += card.interval.name().toLowerCase();
                else
                    shareText += card.backCount + " " + card.interval.name().toLowerCase() + "s";

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                shareIntent.setType("text/plain");
                baseContext.startActivity(Intent.createChooser(shareIntent,
                        baseContext.getResources().getString(R.string.share_count)));
            }
        });
    }

    /**
     *
     */
    public void bindHolder(TimeCard card) {
        this.card = card;

        String label = LabelUtils.last(card.interval, card.backCount);
        title.setText(label + cache.count);

        String axis = "Hour";
        if (card.backCount == 1) {
            if (card.interval != TimeInterval.Day)
                axis = "Day";
        } else
            axis = card.interval.name();
        graph.setAxis(axis);
        graph.setData(cache.data);

        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) graph.getLayoutParams();
        if(!card.collapsed) {
            action.setImageResource(R.drawable.card_collapse);
            params.bottomMargin = 0;
        } else {
            // Do the opposite check even though the xml layout defines the negative margin as such.
            // This is because views are recycled.
            action.setImageResource(R.drawable.card_expand);
            params.bottomMargin = baseContext.getResources().getDimensionPixelSize(R.dimen.graph_margin_bottom);
        }
        graph.setLayoutParams(params);

        setListeners();
    }

    /**
     *
     * @param cardEventListener
     */
    public void setCardEventListener(TimeCardEventListener cardEventListener) {
        this.cardEventListener = cardEventListener;
    }
}
