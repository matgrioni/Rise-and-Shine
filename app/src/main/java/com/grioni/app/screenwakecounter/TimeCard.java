package com.grioni.app.screenwakecounter;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by Matias Grioni on 12/30/14.
 */
public class TimeCard implements Parcelable {
    public TimeInterval interval;
    public int backCount;

    public int count;
    public List<Integer> points;

    public boolean collapsed;

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
     * @param count
     * @param points
     * @param collapsed
     */
    public TimeCard(TimeInterval interval, int backCount, int count,
                    List<Integer> points, boolean collapsed) {
        this.interval = interval;
        this.backCount = backCount;
        this.count = count;
        this.points = points;
        this.collapsed = collapsed;
    }

    /**
     *
     * @param card
     */
    public TimeCard(TimeCard card) {
        this(card.interval, card.backCount, card.count, card.points, card.collapsed);
    }

    /**
     *
     * @param interval
     * @param backCount
     * @param count
     * @param points
     */
    public TimeCard(TimeInterval interval, int backCount, int count, List<Integer> points) {
        this(interval, backCount, count, points, true);
    }

    /**
     *
     * @param interval
     * @param backCount
     */
    public TimeCard(TimeInterval interval, int backCount) {
        this(interval, backCount, -1, null);
    }

    /**
     *
     */
    public TimeCard() {
        this(TimeInterval.Hour, -1, -1, null);
    }

    /**
     *
     * @param in
     */
    public TimeCard(Parcel in) {
        interval = TimeInterval.valueOf(in.readString());
        backCount = in.readInt();
        count = in.readInt();

        int[] pointArray = new int[0];
        in.readIntArray(pointArray);
        for(int i = 0; i < pointArray.length; i++)
            points.set(i, pointArray[i]);

        collapsed = in.readInt() == 1;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(interval.name());
        dest.writeInt(backCount);
        dest.writeInt(count);
        dest.writeIntArray(pointsToArray());
        dest.writeInt(collapsed ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     *
     * @return
     */
    private int[] pointsToArray() {
        int[] array = new int[points.size()];
        for(int i = 0; i < points.size(); i++) {
            array[i] = points.get(i);
        }

        return array;
    }
}
