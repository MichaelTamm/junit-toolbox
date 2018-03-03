package com.googlecode.junittoolbox;

public class PropertyContainer {

    private static PropertyContainer propertyContainer;
    private String parallelType = "methods";

    private PropertyContainer() { }

    public static PropertyContainer getPropertyContainer() {
        if (propertyContainer == null) {
            propertyContainer = new PropertyContainer();
        }
        return propertyContainer;
    }

    public void setParallelType(String parallelType) {
        this.parallelType = parallelType;
    }

    public boolean isParallelTypeClasses() {
        return parallelType.equals("classes");
    }
}
