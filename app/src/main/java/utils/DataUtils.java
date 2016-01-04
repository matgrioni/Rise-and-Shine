package utils;

import java.util.Collection;

/**
 * @author Matias Grioni
 * @created 1/1/16
 *
 * Data computational utility functions.
 */
public class DataUtils {
    /**
     * Sums all terms in the data set.
     *
     * @param data The data set.
     * @return The sum of the terms in the data set.
     */
    public static int sum(Collection<Integer> data) {
        int sum = 0;

        for(int datum : data) {
            sum += datum;
        }

        return sum;
    }

    /**
     * Computes the average for the given data set.
     *
     * @param data The data set.
     * @return The average of the data set.
     */
    public static double average(Collection<Integer> data) {
        return (double) sum(data) / data.size();
    }

    /**
     * Computes the standard deviation for the given data set.
     *
     * @param data The data set.
     * @return The standard deviation of the data set.
     */
    public static double stdev(Collection<Integer> data) {
        double average = average(data);

        double sqsum = 0;
        for(int datum : data) {
            double diff = datum - average;

            sqsum += diff * diff;
        }

        return Math.sqrt(sqsum / data.size());
    }
}
