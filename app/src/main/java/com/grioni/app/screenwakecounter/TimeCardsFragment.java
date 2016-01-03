package com.grioni.app.screenwakecounter;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.TimeCard;
import models.TimeCardCache;
import models.TimeInterval;

/**
 * @author Matias Grioni
 * @created 1/2/15
 *
 * The Fragment that has the {@code TimeCard}s displayed to the user. A {@code TimeCard} will show
 * the counts for that given {@code TimeInterval} and back count, along with an expandable
 * {@code GraphView}.
 */
public class TimeCardsFragment extends Fragment {

    /**
     * @author Matias Grioni
     * @created 1/2/16
     *
     * An interface for when a card clicked. This allows the host {@code Activity} to get notice
     * of when an item in this {@code Fragment} is clicked.
     */
    public interface OnCardClickedListener {
        /**
         * Called when an item in this {@code Fragment} is clicked.
         *
         * @param card The {@code card} that was clicked.
         * @param cardCache The associated {@code TimeCardCache} data associated with this card.
         */
        void onCardClicked(TimeCard card, TimeCardCache cardCache);
    }

    private TimeCardEventListener cardEventListener = new TimeCardEventListener() {
        @Override
        public void onCardClicked(long id) {
            TimeCard card = cardsManager.getCard(id);
            TimeCardCache cardCache = cache.get(card);

            cardClickedListener.onCardClicked(card, cardCache);
        }

        @Override
        public void onCardStateChanged(long id) {
            cardsManager.changeCardState(id);
        }
    };


    ItemTouchHelper.SimpleCallback swipeListener =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT |  ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder2) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int i) {
            int position = viewHolder.getAdapterPosition();
            TimeCard card = cardsAdapter.deleteCard(position);
            cardsManager.remove(card.id);
        }
    };

    private OnCardClickedListener cardClickedListener;

    private TimeCardAdapter cardsAdapter;

    private ScreenCountDatabase countDatabase;
    private TimeCardsManager cardsManager;
    private Map<TimeCard, TimeCardCache> cache;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            cardClickedListener = (OnCardClickedListener) activity;
        } catch (ClassCastException ex) {
            throw new ClassCastException("Host activity must implement OnCardClickedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        cardsManager = ((InstanceApplication) getActivity().getApplicationContext()).getCardsManager();
        countDatabase = ((InstanceApplication) getActivity().getApplicationContext()).getCountDatabase();

        cache = new HashMap<>();
        for(TimeCard card : cardsManager.getCards()) {
            List<Integer> counts = countDatabase.getEntries(card.interval, card.backCount);

            TimeCardCache cardCache = new TimeCardCache(counts);
            cache.put(card, cardCache);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View timeCardsView = inflater.inflate(R.layout.fragment_time_cards, container, false);

        RecyclerView cardsRecycler = (RecyclerView) timeCardsView.findViewById(R.id.time_cards_recycler);
        cardsRecycler.addItemDecoration(new TimeCardItemBottomBorder(getActivity()));
        cardsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        cardsRecycler.setItemAnimator(new DefaultItemAnimator());

        ItemTouchHelper touchHelper = new ItemTouchHelper(swipeListener);
        touchHelper.attachToRecyclerView(cardsRecycler);

        cardsAdapter = new TimeCardAdapter(getActivity(),
                cardsManager.getCards(), cache, cardEventListener);
        cardsRecycler.setAdapter(cardsAdapter);

        update();

        return timeCardsView;
    }

    /**
     * Updates the card adapter with the cards from the TimeCardsManager. Also updates the
     * GraphDetailFragment if any is visible.
     */
    public void update() {
        for (TimeCard card : cardsManager.getCards()) {
            List<Integer> points = countDatabase.getEntries(card.interval, card.backCount);

            TimeCardCache cardCache = new TimeCardCache(points);
            cache.put(card, cardCache);
        }

        cardsAdapter.update(cardsManager.getCards(), cache);
    }

}
