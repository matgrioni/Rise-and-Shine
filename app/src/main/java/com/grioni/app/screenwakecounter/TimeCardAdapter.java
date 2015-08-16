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
    private TimeCardEventListener cardEventListener;

    private List<TimeCard> cards;
    private Context baseContext;

    public TimeCardAdapter(Context context, List<TimeCard> cards,
                           TimeCardEventListener cardEventListener) {
        baseContext = context;
        this.cards = new ArrayList<TimeCard>(cards);
        this.cardEventListener = cardEventListener;
    }

    /**
     *
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public TimeCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cardView = LayoutInflater.from(baseContext)
                .inflate(R.layout.time_card, parent, false);
        TimeCardHolder holder = new TimeCardHolder(cardView);
        holder.setCardEventListener(cardEventListener);

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
        cards.remove(cards.get(position));
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
