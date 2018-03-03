package com.googlecode.junittoolbox;

import java.util.concurrent.atomic.AtomicReference;

public class PropertyContainer {

    private static PropertyContainer propertyContainer;
    private final AtomicReference<String> parallelType = new AtomicReference<>();

    private PropertyContainer() {
        this.parallelType.set("methods");
    }

    public static PropertyContainer getPropertyContainer() {
        if (propertyContainer == null) {
            propertyContainer = new PropertyContainer();
        }
        return propertyContainer;
    }

    public void setParallelType(String parallelType) {
        this.parallelType.set(parallelType);
    }

    public boolean isParallelTypeClasses() {
        return this.parallelType.get().equals("classes");
    }
}
