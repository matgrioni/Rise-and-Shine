package utils;

import models.TimeInterval;

/**
 * @author - Matias Grioni
 * @created - 12/23/15
 *
 * A utility class with static methods to handle the manipulation of labels
 * in the app, such as the notification label.
 */
public class LabelUtils {
    /**
     * Creates a label of the form "Last {@code backCount} {@code interval}: "
     *
     * @param interval - The interval to use in the label.
     * @param backCount - The amount of intervals to go back.
     * @return A label of the appropriate form with filled in values and
     *         appropriate plurals.
     */
    public static String last(TimeInterval interval, int backCount) {
        String label = "Last ";
        if(backCount != 1)
            label += backCount + " " + interval.name() + "s: ";
        else
            label += interval.name() + ": ";

        return label;
    }
}
