package com.grioni.app.screenwakecounter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

/**
 * Created by Matias Grioni on 1/2/15.
 */
public class TimeCardsFragment extends Fragment
    implements GraphDetailFragment.OnCardDeletedListener,
               AddCardDialogFragment.OnCardAddedListener {

    private TimeCardEventListener cardEventListener = new TimeCardEventListener() {
        @Override
        public void onCardClicked(int position) {
            graphDetails = GraphDetailFragment.newInstance(position);

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_parent, graphDetails, "graphDetails");
            transaction.addToBackStack(null);
            transaction.commit();

            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        @Override
        public void onCardStateChanged(int position) {
            timeCardsManager.changeCardState(position);
        }
    };

    private View.OnClickListener onAddCard = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AddCardDialogFragment addCardDialog = new AddCardDialogFragment();
            FragmentManager manager = getChildFragmentManager();
            addCardDialog.show(manager, "add_card");
        }
    };

    private FragmentManager.OnBackStackChangedListener backStackChanged = new FragmentManager.OnBackStackChangedListener() {
        @Override
        public void onBackStackChanged() {
            // If the backstack changed and the GraphDetailsFragment was visible then it no longer is.
            if(graphDetails != null)
                graphDetails = null;
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
            timeCardsManager.remove(position);
            cardsAdapter.deleteCard(position);
        }
    };

    private ActionBar actionBar;
    private TimeCardAdapter cardsAdapter;

    private GraphDetailFragment graphDetails;
    private FloatingActionButton fab;
    private Animation fabIn;
    private Animation fabOut;

    private TimeCardsManager timeCardsManager;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Get the ActionBar from the Activity here rather than in onAttach
        // since on a screen rotate or other Activity recreation, the ActionBar
        // will be none.
        try {
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        } catch (ClassCastException ex) {
            throw new ClassCastException("Activity must be ActionbarActivity");
        }

        Fragment g = getChildFragmentManager().findFragmentByTag("graphDetails");
        if (g != null) {
            graphDetails = (GraphDetailFragment) g;
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getChildFragmentManager().addOnBackStackChangedListener(backStackChanged);

        timeCardsManager = TimeCardsManager.getInstance(getActivity());

        SharedPreferences sharedPreferences = getActivity()
                .getSharedPreferences(getString(R.string.shared_preference_file), Context.MODE_PRIVATE);
        boolean init = sharedPreferences.getBoolean(getString(R.string.cards_init), false);

        // If this is the first time the program is run, then init will be false, the default value,
        // since no value for cards_init has been written yet. Then the 3 default cards will be added.
        if(!init) {
            timeCardsManager.addCard(new TimeCard(TimeInterval.Day, 1));
            timeCardsManager.addCard(new TimeCard(TimeInterval.Week, 1));
            timeCardsManager.addCard(new TimeCard(TimeInterval.Month, 1));

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(getString(R.string.cards_init), true);
            editor.apply();
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
                timeCardsManager.getCards(), cardEventListener);
        cardsRecycler.setAdapter(cardsAdapter);

        reload();

        fab = (FloatingActionButton) timeCardsView.findViewById(R.id.fab_add_time_card);
        fab.setOnClickListener(onAddCard);

        fabIn = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
        fabOut = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);

        return timeCardsView;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getChildFragmentManager().popBackStack();
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setTitle(R.string.app_name);

                graphDetails = null;

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onCardAdded(TimeCard card) {
        // Since the card that was added is unqueried/cache is empty, we have
        // to query the card which would be the last card in the list since it
        // was just added.
        timeCardsManager.queryLast();
        cardsAdapter.addCard(card);
    }

    public void onCardDeleted(int position, TimeCard card) {
        cardsAdapter.deleteCard(position);

        // Prior this following code checked if the GraphDetailsFragment was
        // null before popping it off the backstack since this callback could
        // be used by either the TimeCardHolder item or the GraphDetailFragment
        // from the menu options. Since the callback is now only called from
        // the GraphDetailFragment we can be assured that there was a
        // GraphDetailFragment.
        getChildFragmentManager().popBackStack();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(R.string.app_name);

        fab.startAnimation(fabIn);
        fab.setVisibility(View.VISIBLE);

        graphDetails = null;
    }

    /**
     *
     */
    public void update() {
        cardsAdapter.update(timeCardsManager.getCards());

        if(graphDetails != null)
            graphDetails.update();
    }

    /**
     *
     */
    public void reload() {
        timeCardsManager.queryAll();
        update();
    }
}
