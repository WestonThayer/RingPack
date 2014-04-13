package com.cryclops.ringpack.services;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class AppServiceLocator implements ServiceLocator {
    private static AppServiceLocator ourInstance = new AppServiceLocator();

    public static AppServiceLocator getInstance() {
        return ourInstance;
    }

    private Map<Class, Object> map;

    private AppServiceLocator() {
        map = new HashMap<Class, Object>();

        addService(PackReaderService.class, new FilePackReaderService());
        //addService(PackReaderService.class, new MockPackReaderService());
    }

    @Override
    public Object getService(Class type) {
        return map.get(type);
    }

    @Override
    public void addService(Class type, Object obj) {
        map.put(type, obj);
    }

    @Override
    public void removeService(Class type) {
        if (map.containsKey(type)) {
            map.remove(type);
        }
    }

    public String getResString(int id) {
        ResourceService r = (ResourceService)this.getService(ResourceService.class);
        if (r != null) {
            return r.getString(id);
        }

        return null;
    }
}
