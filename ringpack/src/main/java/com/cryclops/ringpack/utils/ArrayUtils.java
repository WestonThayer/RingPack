package com.cryclops.ringpack.utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Find stuff in arrays.
 */
public class ArrayUtils {

    /**
     * Check to see if the given object is contained in the object array.
     * @param objects The object array to check
     * @param obj The object to look for
     * @return True if found
     */
    public static <T> boolean contains(T[] objects, T obj) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i].equals(obj)) {
                return  true;
            }
        }

        return false;
    }

    /**
     * Find the first item in the array that matches the given condition.
     * @param array
     * @param selector
     * @param <T>
     * @return The item, or null if not found
     */
    public static <T> T firstOrDefault(T[] array, PropertySelector<T> selector) {
        for (T item : array) {
            if (selector.test(item)) {
                return item;
            }
        }

        return null;
    }

    /**
     * Convert the given array into an ArrayList.
     * @param array
     * @param <T>
     * @return
     */
    public static <T> ArrayList<T> toArrayList(T[] array) {
        ArrayList<T> al = new ArrayList<T>();

        for (T item : array) {
            al.add(item);
        }

        return al;
    }
}
