package com.grioni.app.screenwakecounter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Matias Grioni on 12/27/14.
 */
public class TimeCardAdapter extends RecyclerView.Adapter<TimeCardHolder> {

    private TimeCardsFragment.TimeCardDeleteListener cardDeleteListener;
    private TimeCardsFragment.TimeCardExpandListener cardExpandListener;
    private TimeCardsFragment.TimeCardStateListener cardStateListener;

    private List<TimeCard> cards;
    private Context baseContext;

    /**
     *
     * @param activity
     * @param cards
     */
    public TimeCardAdapter(Activity activity, List<TimeCard> cards) {
        this.cards = new ArrayList<TimeCard>(cards);

        try {
            cardDeleteListener = (TimeCardsFragment.TimeCardDeleteListener) activity;
            cardExpandListener = (TimeCardsFragment.TimeCardExpandListener) activity;
            cardStateListener = (TimeCardsFragment.TimeCardStateListener) activity;
        } catch(ClassCastException ex) {
            throw new ClassCastException(activity.toString() +
                    " must implement TimeCardDeleteListener, TimeCardExpandListener, and" +
                    " TimeCardStateListener");
        }
    }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public TimeCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(baseContext == null)
            baseContext = parent.getContext();

        View cardView = LayoutInflater.from(baseContext)
                .inflate(R.layout.time_card, parent, false);
        TimeCardHolder holder = new TimeCardHolder(cardView);
        holder.setCardExpandListener(cardExpandListener);
        holder.setCardDeleteListener(cardDeleteListener);
        holder.setCardStateListener(cardStateListener);

        return holder;
    }

    /**
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(TimeCardHolder holder, int position) {
        holder.bindHolder(cards.get(position));
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    /**
     *
     * @return
     */
    @Override
    public int getItemCount() {
        return cards.size();
    }

    /**
     *
     * @param position
     * @param card
     */
    public void updateCard(int position, TimeCard card) {
        cards.set(position, card);
        notifyItemChanged(position);
    }

    /**
     *
     * @param position
     */
    public void deleteCard(int position) {
        cards.remove(position);
        notifyItemRemoved(position);
    }

    /**
     *
     * @param card
     */
    public void addCard(TimeCard card) {
        cards.add(new TimeCard(card));
        notifyItemInserted(cards.size() - 1);
    }

    /**
     *
     * @param cards
     */
    public void update(List<TimeCard> cards) {
        this.cards = new ArrayList<TimeCard>(cards);
        notifyDataSetChanged();
    }
}
