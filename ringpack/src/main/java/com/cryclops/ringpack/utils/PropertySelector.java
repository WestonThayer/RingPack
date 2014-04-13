package com.cryclops.ringpack.utils;

/**
 * Used to selected a property on an object.
 */
public interface PropertySelector<T> {

    /**
     * Check to see if the item meets some conditions.
     * @param item
     * @return True if it meets the conditions
     */
    boolean test(T item);
}
