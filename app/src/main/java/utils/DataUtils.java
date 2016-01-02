package utils;

import java.util.Collection;

/**
 * @author Matias Grioni
 * @created 1/1/16
 */
public class DataUtils {
    public static int sum(Collection<Integer> data) {
        int sum = 0;

        for(int datum : data) {
            sum += datum;
        }

        return sum;
    }
}
