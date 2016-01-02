package com.grioni.app.screenwakecounter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.TimeCard;
import models.TimeCardCache;

/**
 * Created by Matias Grioni on 12/27/14.
 */
public class TimeCardAdapter extends RecyclerView.Adapter<TimeCardHolder> {
    private TimeCardEventListener cardEventListener;

    private List<TimeCard> cards;
    private Map<TimeCard, TimeCardCache> caches;
    private Context baseContext;

    public TimeCardAdapter(Context context, List<TimeCard> cards, Map<TimeCard, TimeCardCache> caches,
                           TimeCardEventListener cardEventListener) {
        baseContext = context;
        this.cards = new ArrayList<>(cards);
        this.caches = new HashMap<>(caches);
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
        TimeCard card = cards.get(position);
        holder.setCache(caches.get(card));
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
     * @param cache
     */
    public void updateCard(int position, TimeCard card, TimeCardCache cache) {
        cards.set(position, card);
        caches.put(card, cache);

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
     * @param cache
     */
    public void update(List<TimeCard> cards, Map<TimeCard, TimeCardCache> cache) {
        this.cards = new ArrayList<>(cards);
        this.caches = new HashMap<>(cache);
        notifyDataSetChanged();
    }
}
