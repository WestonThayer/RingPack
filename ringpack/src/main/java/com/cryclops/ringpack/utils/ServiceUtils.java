package com.cryclops.ringpack.utils;

import com.cryclops.ringpack.services.AppServiceLocator;
import com.cryclops.ringpack.services.Log;
import com.cryclops.ringpack.services.NotificationService;
import com.cryclops.ringpack.services.PackReaderService;
import com.cryclops.ringpack.services.ResourceService;

/**
 * Short syntax to reach a Service.
 */
public class ServiceUtils {

    public static Log getLog() {
        Log log = (Log) AppServiceLocator.getInstance().getService(Log.class);
        return log;
    }

    public static NotificationService getNotification() {
        NotificationService notificationService = (NotificationService) AppServiceLocator.getInstance().getService(NotificationService.class);
        return notificationService;
    }

    public static ResourceService getResource() {
        ResourceService resourceService = (ResourceService) AppServiceLocator.getInstance().getService(ResourceService.class);
        return resourceService;
    }

    public static PackReaderService getPackReader() {
        PackReaderService readerService = (PackReaderService) AppServiceLocator.getInstance().getService(PackReaderService.class);
        return readerService;
    }
}
