package com.cryclops.ringpack.utils;

import java.util.List;

/**
 * Helpers for lists.
 */
public class ListUtils {

    /**
     * Find the first item in the list that matches the given condition.
     * @param list
     * @param selector
     * @param <T>
     * @return The item, or null if not found
     */
    public static <T> T firstOrDefault(List<T> list, PropertySelector<T> selector) {
        for (T item : list) {
            if (selector.test(item)) {
                return item;
            }
        }

        return null;
    }

    /**
     * Replace an Object in the List with a new Object.
     * @param list
     * @param oldValue
     * @param newValue
     * @param <T>
     */
    public static <T> void replace(List<T> list, T oldValue, T newValue) {
        int i = list.indexOf(oldValue);
        list.set(i, newValue);
    }
}
