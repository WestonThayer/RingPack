package com.cryclops.ringpack.services;

/**
 * Describes a class that holds many service objects for dependency injection.
 */
public interface ServiceLocator {

    Object getService(Class type);
    void addService(Class type, Object obj);
    void removeService(Class type);
}
