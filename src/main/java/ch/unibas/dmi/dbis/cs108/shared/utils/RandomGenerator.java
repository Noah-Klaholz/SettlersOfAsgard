package ch.unibas.dmi.dbis.cs108.shared.utils;

import java.util.List;
import java.util.Random;

/**
 * RandomGenerator is a utility class that provides methods for generating random
 * numbers and making random choices.
 * It includes methods for generating random integers, doubles, and selecting
 * random elements from arrays or lists.
 */
public class RandomGenerator {
    /** Object of the type random */
    private static final Random RANDOM = new Random();

    /**
     * Returns true approximately 'percentage' percent of the time.
     * Ex.: if percentage is 30.0, returns true ~30% of the time.
     *
     * @param percentage the percentage chance of returning true
     *   @return true if the random chance is met, false otherwise
     */
    public static boolean chance(int percentage) {
        return RANDOM.nextDouble() * 100.0 < percentage;
    }

    /**
     * Returns a random integer between 'min' and 'max' (inclusive).
     *
     * @param min the minimum value (inclusive)
     *  @param max the maximum value (inclusive)
     *    @return a random integer between min and max
     */
    public static int randomIntInRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must not exceed max.");
        }
        return min + RANDOM.nextInt((max - min) + 1);
    }

    /**
     * Returns a random double between 'min' and 'max' (inclusive).
     *
     * @param min the minimum value (inclusive)
     *          @param max the maximum value (inclusive)
     *
     *
     *            @return a random double between min and max
     *
     */
    public static double randomDoubleInRange(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException("min must not exceed max.");
        }
        return min + (RANDOM.nextDouble() * (max - min));
    }

    /**
     * Returns a random element from an array or null if array is empty.
     *
     * @param array the array to pick a random element from
     *
     * @return a random element from the array or null if the array is empty
     */
    public static <T> T pickRandomElement(T[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        int index = RANDOM.nextInt(array.length);
        return array[index];
    }

    /**
     * Returns a random element from a list or null if list is empty.
     *
     * @param array the list to pick a random element from
     *       @return a random element from the list or null if the list is empty
     */
    public static <T> T pickRandomElement(List<T> array) {
        if (array == null || array.isEmpty()) {
            return null;
        }
        int index = RANDOM.nextInt(array.size());
        return array.get(index);
    }
}