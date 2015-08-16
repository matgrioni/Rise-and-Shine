package com.grioni.app.screenwakecounter;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Matias Grioni on 12/30/14.
 */
public class TimeCard implements Parcelable {
    /**
     * @author - Matias Grioni
     * @created - 8/15/15
     */
    public class Cache {
        public int count;
        public List<Integer> points;
    }

    public TimeInterval interval;
    public int backCount;
    public boolean collapsed;

    public Cache cache;

    public static final Creator CREATOR = new Creator<TimeCard>() {
        public TimeCard createFromParcel(Parcel in) {
            return new TimeCard(in);
        }

        public TimeCard[] newArray(int size) {
            return new TimeCard[size];
        }
    };

    /**
     *
     * @param interval
     * @param backCount
     * @param collapsed
     */
    public TimeCard(TimeInterval interval, int backCount, boolean collapsed) {
        this.interval = interval;
        this.backCount = backCount;
        this.collapsed = collapsed;
        this.cache = new Cache();
    }

    /**
     *
     * @param card
     */
    public TimeCard(TimeCard card) {
        this(card.interval, card.backCount, card.collapsed);
        this.cache = card.cache;
    }

    /**
     *
     * @param interval
     * @param backCount
     */
    public TimeCard(TimeInterval interval, int backCount) {
        this(interval, backCount, true);
    }

    /**
     *
     */
    public TimeCard() {
        this(TimeInterval.Hour, -1);
    }

    /**
     *
     * @param in
     */
    public TimeCard(Parcel in) {
        interval = TimeInterval.valueOf(in.readString());
        backCount = in.readInt();
        collapsed = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(interval.name());
        dest.writeInt(backCount);
        dest.writeInt(collapsed ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
