package com.cryclops.ringpack.services;

/**
 * Expose resources.
 */
public interface ResourceService {

    String getString(int id);
    String getString(int resId, Object... formatArgs);
}
