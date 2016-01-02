package com.grioni.app.screenwakecounter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import models.TimeCard;
import models.TimeCardCache;
import services.ScreenCountService;
import services.ScreenCountWriteService;
import services.ServiceUpdateListener;
import views.FloatingActionButton;

/**
 * Created by Matias Grioni on 12/16/14.
 */
public class MainActivity extends AppCompatActivity implements
        AddCardDialogFragment.OnCardAddedListener,
        GraphDetailFragment.OnCardDeletedListener,
        TimeCardsFragment.OnCardClickedListener {

    /**
     * @author Matias Grioni
     * @created 1/1/16
     */
    private enum FragmentState {
        TIME_CARDS, GRAPH_DETAILS, SETTINGS, UNDEFINED
    }

    private ServiceUpdateListener onScreenWake = new ServiceUpdateListener() {
        @Override
        public void onUpdate() {
            updateInfo(ScreenCountService.getHourCount());

            if (fragmentState == FragmentState.TIME_CARDS)
                timeCards.update();
            else if (fragmentState == FragmentState.GRAPH_DETAILS)
                graphDetails.update();
        }
    };

    private ServiceUpdateListener onWrite = new ServiceUpdateListener() {
        @Override
        public void onUpdate() {
            // Update the notification only here, because when the screen is woken, the
            // ScreenCountService automatically updates the notification, but when the database
            // is written to every hour it does not update since the notification is part of the
            // foreground service. This will update the foreground service notification.
            updateInfo(0);

            if (fragmentState == FragmentState.TIME_CARDS)
                timeCards.update();
            else if (fragmentState == FragmentState.GRAPH_DETAILS)
                graphDetails.update();
        }
    };

    private boolean countBound;
    private ScreenCountService countService;
    private ServiceConnection countConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ScreenCountService.ScreenCountBinder binder = (ScreenCountService.ScreenCountBinder) service;
            countService = binder.getService();

            countService.setUpdateListener(onScreenWake);
            countBound = true;

            updateInfo(ScreenCountService.getHourCount());
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            countService = null;
            countBound = false;
        }
    };

    private boolean writeBound;
    private ScreenCountWriteService writeService;
    private ServiceConnection writeConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            ScreenCountWriteService.ScreenCountWriteBinder binder =
                    (ScreenCountWriteService.ScreenCountWriteBinder) service;
            writeService = binder.getService();

            writeService.setUpdateListener(onWrite);
            writeBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            writeService = null;
            writeBound = false;
        }
    };

    private View.OnClickListener onAddCard = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AddCardDialogFragment addCardDialog = new AddCardDialogFragment();
            FragmentManager manager = getFragmentManager();
            addCardDialog.show(manager, "add_card");
        }
    };

    private TextView hourCount;
    private TextView countdown;

    private FloatingActionButton fab;
    private Animation fabIn;
    private Animation fabOut;

    private FragmentState fragmentState;
    private TimeCardsFragment timeCards;
    private GraphDetailFragment graphDetails;
    private SettingsFragment settings;

    private boolean timerFinished = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent wakeIntent = new Intent(MainActivity.this, ScreenCountService.class);
        bindService(wakeIntent, countConnection, Context.BIND_AUTO_CREATE);

        Intent writeIntent = new Intent(MainActivity.this, ScreenCountWriteService.class);
        bindService(writeIntent, writeConnection, Context.BIND_AUTO_CREATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if(toolbar != null)
            setSupportActionBar(toolbar);

        setupFragments();

        hourCount = (TextView) findViewById(R.id.hour_count);
        countdown = (TextView) findViewById(R.id.countdown);
        fab = (FloatingActionButton) findViewById(R.id.fab_add_time_card);

        fab.setOnClickListener(onAddCard);
        fabIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        fabOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                popBackStack();

                break;

            case R.id.settings:
                settings = new SettingsFragment();
                addToBackStack(settings, "settings", FragmentState.SETTINGS);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (fragmentState == FragmentState.SETTINGS || fragmentState == FragmentState.GRAPH_DETAILS) {
            popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(countBound) {
            unbindService(countConnection);
            countBound = false;
        }

        if(writeBound) {
            unbindService(writeConnection);
            writeBound = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onCardClicked(int position, TimeCardCache cache) {
        graphDetails = GraphDetailFragment.newInstance(position, cache);
        addToBackStack(graphDetails, "graphDetails", FragmentState.GRAPH_DETAILS);
    }

    @Override
    public void onCardAdded(TimeCard card) {
        timeCards.update();
    }

    @Override
    public void onCardDeleted(int position, TimeCard card) {
        popBackStack();
    }

    /**
     *
     * @param count
     */
    private void updateInfo(int count) {
        hourCount.setText(Integer.toString(count));

        long timeLeft = 60 * 60 * 1000 -
                (SystemClock.elapsedRealtime() - ScreenCountService.getLastAlarmTime());
        CountDownTimer timer = new CountDownTimer(timeLeft, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;

                countdown.setText(minutes + "m " + seconds + "s");
                timerFinished = false;
            }

            @Override
            public void onFinish() {
                timerFinished = true;
            }
        };

        // If the timer is timerFinished, meaning that the last timer has run through and timerFinished
        // and that there would not be two timers affecting the timer TextView, then start it.
        if(timerFinished)
            timer.start();
    }

    /**
     * Sets up the main {@code Fragment} for this {@code Activity}. Through a rotation or activity
     * recreation the activity state has to be recreated. This puts the proper {@code Fragment} as
     * it needs to be.
     */
    private void setupFragments() {
        fragmentState = FragmentState.UNDEFINED;

        Fragment s = getFragmentManager().findFragmentByTag("settings");
        if (s != null) {
            settings = (SettingsFragment) s;
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            fragmentState = FragmentState.SETTINGS;
        }

        if (fragmentState == FragmentState.UNDEFINED) {
            Fragment g = getFragmentManager().findFragmentByTag("graphDetails");
            if (g != null) {
                graphDetails = (GraphDetailFragment) g;
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                fragmentState = FragmentState.GRAPH_DETAILS;
            }
        }

        if (fragmentState == FragmentState.UNDEFINED) {
            Fragment t = getFragmentManager().findFragmentByTag("timeCards");
            if (t != null) {
                timeCards = (TimeCardsFragment) t;
            } else {
                timeCards = new TimeCardsFragment();

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.add(R.id.time_cards_container, timeCards, "timeCards");
                transaction.commit();
            }

            fragmentState = FragmentState.TIME_CARDS;
        }
    }

    /**
     *
     */
    private void popBackStack() {
        getFragmentManager().popBackStack();
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        fragmentState = FragmentState.TIME_CARDS;
        timeCards.update();

        fab.startAnimation(fabIn);
        fab.setVisibility(View.VISIBLE);
    }

    /**
     *
     * @param f
     * @param tag
     */
    private void addToBackStack(Fragment f, String tag, FragmentState newState) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.time_cards_container, f, tag);
        transaction.addToBackStack(null);
        transaction.commit();

        fragmentState = newState;

        fab.startAnimation(fabOut);
        fab.setVisibility(View.GONE);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}