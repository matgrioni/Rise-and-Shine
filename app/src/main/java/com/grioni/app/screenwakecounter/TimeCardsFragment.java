package com.grioni.app.screenwakecounter;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Matias Grioni on 1/2/15.
 */
public class TimeCardsFragment extends Fragment {

    /**
     *
     */
    public interface TimeCardExpandListener {
        public void expandTimeCard(TimeCard card, int position);
    }

    /**
     *
     */
    public interface TimeCardDeleteListener {
        public void onCardDelete(int id);
    }

    /**
     *
     */
    public interface TimeCardStateListener {
        public void onCardChangeState(int position);
    }

    private RecyclerView cardsRecycler;
    private TimeCardAdapter cardsAdapter;

    private TimeCardsDatabase timeCardsDatabase;
    private ScreenCountDatabase countDatabase;

    private List<TimeCard> cards;
    private int startSize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        countDatabase = ScreenCountDatabase.getInstance(getActivity());
        countDatabase.open();

        timeCardsDatabase = TimeCardsDatabase.getInstance(getActivity());

        timeCardsDatabase.open();
        cards = timeCardsDatabase.getCards();

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(getString(R.string.shared_preference_file), Context.MODE_PRIVATE);
        boolean init = sharedPreferences.getBoolean(getString(R.string.cards_init), false);

        // If this is the first time the program is run, then init will be false, the default value,
        // since no value for cards_init has been written yet. Then the 3 default cards will be added.
        if(!init) {
            cards.add(new TimeCard(TimeInterval.Day, 1));
            cards.add(new TimeCard(TimeInterval.Week, 1));
            cards.add(new TimeCard(TimeInterval.Month, 1));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.cards_init), true);
            editor.commit();

            for(int i = 0; i < cards.size(); i++) {
                timeCardsDatabase.addCard(cards.get(i));
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View timeCardsView = inflater.inflate(R.layout.fragment_time_cards, container, false);

        cardsRecycler = (RecyclerView) timeCardsView.findViewById(R.id.time_cards_recycler);
        cardsRecycler.addItemDecoration(new CardItemDecorator(getActivity()));
        cardsRecycler.setLayoutManager(new LinearLayoutManager(getActivity()));
        cardsRecycler.setItemAnimator(new DefaultItemAnimator());

        cardsAdapter = new TimeCardAdapter(getActivity(), cards);
        cardsRecycler.setAdapter(cardsAdapter);

        updateCards();

        return timeCardsView;
    }

    /**
     *
     * @param card
     */
    public void addCard(TimeCard card) {
        cards.add(card);
        cardsAdapter.addCard(card);
        timeCardsDatabase.addCard(card);
    }

    /**
     *
     * @param position
     * @return
     */
    public TimeCard getCard(int position) {
        return cards.get(position);
    }

    /**
     *
     * @param position
     */
    public void changeCardState(int position) {
        TimeCard card = cards.get(position);
        card.collapsed = !card.collapsed;

        cards.set(position, card);

        cardsAdapter.updateCard(position, card);
        timeCardsDatabase.updateCard(position, card);
    }

    /**
     *
     */
    public void incrementCards() {
        for(int i = 0; i < cards.size(); i++) {
            TimeCard card = cards.get(i);

            card.count++;

            List<Integer> points = card.points;
            int lastPoint = points.get(points.size() - 1);
            points.set(points.size() - 1, ++lastPoint);

            card.points = points;

            cards.set(i, card);
        }

        cardsAdapter.update(cards);
    }

    /**
     *
     */
    public void updateCards() {
        for(int i = 0; i < cards.size(); i++) {
            TimeCard card = cards.get(i);

            card.points = countDatabase.getCounts(card.interval, card.backCount);
            card.count = sumPoints(card.points);

            cards.set(i, card);
        }

        cardsAdapter.update(cards);
    }

    /**
     *
     * @param position
     */
    public void deleteCard(int position) {
        cards.remove(position);
        cardsAdapter.deleteCard(position);
    }

    /**
     *
     * @param points
     * @return
     */
    private int sumPoints(List<Integer> points) {
        int sum = 0;
        for(int i = 0; i < points.size(); i++)
            sum += points.get(i);

        return sum;
    }
}
