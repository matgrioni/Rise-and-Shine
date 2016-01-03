package models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author - Matias Grioni
 * @created - 12/30/14
 *
 * A TimeCard are the units in the app that detail what counts to keep track of.
 * A TimeCard has a TimeInterval and a count backwards that are combined such
 * that 5 weeks, 3 months, 9 hours, etc, are represented. There is also a flag
 * for if the card is collapsed in the TimeCardAdapter.
 *
 * The cache member is so called because it is data (points and count) that is
 * inherent to the TimeCard but the TimeCard can exist without it being defined.
 * Therefore the cache of the TimeCard are how these values are transferred
 * but there is no guarantee that the data will be correct. For this reason, use
 * TimeCardsManager.query for the desired card to make sure the TimeCard is up
 * to date.
 *
 * Implements Parcelable so that the TimeCard can be written to the database.
 */
public class TimeCard implements Parcelable {
    public static long ID_SIZE;

    static {
        ID_SIZE = 0;
    }

    public long id;
    public TimeInterval interval;
    public int backCount;
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
     * Explicit constructor for the TimeCard.
     *
     * @param interval - The TimeInterval for this card.
     * @param backCount - The back count for the given TimeInterval.
     * @param collapsed - Flag if the card is collapsed or expanded when
     *                  visible in the TimeCardAdapter.
     */
    public TimeCard(TimeInterval interval, int backCount, boolean collapsed) {
        ID_SIZE++;

        this.id = ID_SIZE;
        this.interval = interval;
        this.backCount = backCount;
        this.collapsed = collapsed;
    }

    /**
     * Constructor from another TimeCard.
     *
     * @param card - The TimeCard to create this TimeCard from.
     */
    public TimeCard(TimeCard card) {
        this(card.interval, card.backCount, card.collapsed);
    }

    /**
     * Constructor for a collapsed card.
     *
     * @param interval - The TimeInterval for this card.
     * @param backCount - The back count for the provided TimeInterval.
     */
    public TimeCard(TimeInterval interval, int backCount) {
        this(interval, backCount, true);
    }

    /**
     * Constructor for a TimeCard with a TimeInterval of an hour and back count
     * of -1, and a collapsed state.
     */
    public TimeCard() {
        this(TimeInterval.Hour, -1);
    }

    /**
     * Constructor from a Parcel.
     * @param in - The Parcel to construct this TimeCard from.
     */
    public TimeCard(Parcel in) {
        id = in.readLong();
        interval = TimeInterval.valueOf(in.readString());
        backCount = in.readInt();
        collapsed = in.readInt() == 1;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (!(obj instanceof TimeCard)) {
            return false;
        }

        TimeCard card = (TimeCard) obj;
        return this.id == card.id && this.interval == card.interval &&
                this.backCount == card.backCount && this.collapsed == card.collapsed;
    }

    @Override
    public int hashCode() {
        return backCount;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(interval.name());
        dest.writeInt(backCount);
        dest.writeInt(collapsed ? 1 : 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
