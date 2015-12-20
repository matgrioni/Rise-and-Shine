package models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Matias Grioni
 * @created 12/19/15
 *
 * A convenience class to not have to pass in the total and the actual data entries for each
 * TimeCard. Otherwise, either 2 parameters would have to passed in when the TimeCard data is needed
 * or the data would have to be summed.
 */
public class TimeCardCache implements Parcelable {
    public int count;
    public List<Integer> data;

    public static final Parcelable.Creator<TimeCardCache> CREATOR = new Creator<TimeCardCache>() {
        @Override
        public TimeCardCache createFromParcel(Parcel source) {
            return new TimeCardCache(source);
        }

        @Override
        public TimeCardCache[] newArray(int size) {
            return new TimeCardCache[size];
        }
    };

    public TimeCardCache() {
        this.count = 0;
        this.data = new ArrayList<>();
    }

    /**
     * Constructor that takes in the data for the cache. Does not alias points.
     *
     * @param data
     */
    public TimeCardCache(List<Integer> data) {
        this.data = new ArrayList<>(data);

        for (int i = 0; i < this.data.size(); i++) {
            this.count += data.get(i);
        }
    }

    /**
     * Copy constructor for TimeCardCache.
     *
     * @param cache - The TimeCardCache object to copy.
     */
    public TimeCardCache(TimeCardCache cache) {
        this.count = cache.count;
        this.data = new ArrayList<>(cache.data);
    }

    /**
     *
     * @param in
     */
    public TimeCardCache(Parcel in) {
        count = in.readInt();
        int[] dataArray = in.createIntArray();

        this.data = new ArrayList<>();
        for(int i = 0; i < dataArray.length; i++) {
            this.data.set(i, dataArray[i]);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(count);

        int[] dataArray = new int[data.size()];
        for (int i = 0; i < data.size(); i++) {
            dataArray[i] = data.get(i);
        }

        dest.writeIntArray(dataArray);
    }
}
