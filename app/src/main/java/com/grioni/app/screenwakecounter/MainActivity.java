package com.grioni.app.screenwakecounter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Matias Grioni on 12/16/14.
 */
public class MainActivity extends ActionBarActivity implements
        AddCardDialogFragment.AddCardDialogListener,
        TimeCardsFragment.TimeCardDeleteListener,
        TimeCardsFragment.TimeCardExpandListener,
        TimeCardsFragment.TimeCardStateListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private ScreenWakeListener onScreenWake = new ScreenWakeListener() {
        @Override
        public void onScreenWake() {
            updateInfo(ScreenCountService.getHourCount());
            timeCards.incrementCards();

            if(graphDetails != null) {
                TimeCard card = timeCards.getCard(graphDetails.getPosition());
                graphDetails.update(card);
            }
        }
    };

    private WriteListener onWrite = new WriteListener() {
        @Override
        public void onWrite() {
            updateInfo(0);
            timeCards.updateCards();

            // Update the notification only here, because when the screen is woken, the
            // ScreenCountService automatically updates the notification, but when the database
            // is written to every hour it does not update since the notification is part of the
            // foreground service. This will update the foreground service notification.
            countService.updateNotif();

            if(graphDetails != null) {
                TimeCard card = timeCards.getCard(graphDetails.getPosition());
                graphDetails.update(card);
            }
        }
    };

    private boolean countBound;
    private ScreenCountService countService;
    private ServiceConnection countConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            ScreenCountService.ScreenCountBinder binder = (ScreenCountService.ScreenCountBinder) service;
            countService = binder.getService();

            countService.setScreenWakeListener(onScreenWake);
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

            writeService.setWriteListener(onWrite);
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

    private GraphDetailFragment graphDetails;
    private TimeCardsFragment timeCards;
    private FloatingActionButton fab;

    private Animation fabIn;
    private Animation fabOut;

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
        else {
            Toast.makeText(this, "MAtias", Toast.LENGTH_SHORT).show();
        }

        hourCount = (TextView) findViewById(R.id.hour_count);
        countdown = (TextView) findViewById(R.id.countdown);

        Fragment t = getFragmentManager().findFragmentByTag("timeCards");

        if(t != null) {
            timeCards = (TimeCardsFragment) t;
        } else {
            timeCards = new TimeCardsFragment();

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(R.id.time_cards_container, timeCards, "timeCards");
            transaction.commit();
        }

        Fragment g = getFragmentManager().findFragmentByTag("graphDetails");
        if(g != null) {
            graphDetails = (GraphDetailFragment) g;

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Fragment s = getFragmentManager().findFragmentByTag("settings");
        if(s != null) {
            settings = (SettingsFragment) s;

            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        fab = (FloatingActionButton) findViewById(R.id.fab_add_time_card);
        fab.setOnClickListener(onAddCard);

        fabIn = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        fabOut = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                fab.startAnimation(fabIn);
                fab.setVisibility(View.VISIBLE);

                getFragmentManager().popBackStack();
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                getSupportActionBar().setTitle(R.string.app_name);

                graphDetails = null;
                settings = null;

                break;

            case R.id.settings:
                fab.startAnimation(fabOut);
                fab.setVisibility(View.GONE);

                settings = new SettingsFragment();

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.time_cards_container, settings, "settings");
                transaction.addToBackStack(null);
                transaction.commit();

                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(graphDetails != null || settings != null) {
            fab.startAnimation(fabIn);
            fab.setVisibility(View.VISIBLE);

            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);

            graphDetails = null;
            settings = null;
            getFragmentManager().popBackStack();
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
     *
     * @param position
     */
    public void expandTimeCard(TimeCard card, int position) {
        fab.startAnimation(fabOut);
        fab.setVisibility(View.GONE);

        graphDetails = GraphDetailFragment.newInstance(card, position);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.time_cards_container, graphDetails, "graphDetails");
        transaction.addToBackStack(null);
        transaction.commit();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     *
     * @param position
     */
    public void onCardDelete(int position) {
        if(graphDetails != null) {
            fab.startAnimation(fabIn);
            fab.setVisibility(View.VISIBLE);

            getFragmentManager().popBackStack();
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setTitle(R.string.app_name);

            graphDetails = null;
        }

        timeCards.deleteCard(position);
    }

    /**
     *
     * @param position
     */
    public void onCardChangeState(int position) {
        timeCards.changeCardState(position);
    }

    /**
     *
     * @param card
     */
    public void onCardAdded(TimeCard card) {
        timeCards.addCard(card);
    }

    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        // If the settings that is changed is the notification count, then update the service notif
        // appropriately
        if(key.equals("pref_notification_counts")) {
            String preference = preferences.getString("pref_notification_counts", "1 Hour");

            int split = preference.indexOf(" ");
            int backcount = Integer.parseInt(preference.substring(0, split));
            TimeInterval interval = TimeInterval.valueOf(preference.substring(split + 1));

            countService.setNotifBackcount(backcount);
            countService.setNotifInterval(interval);
        }
    }


}