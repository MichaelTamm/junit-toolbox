package com.googlecode.junittoolbox;

/**
 * Abstract base class to encapsulate assertions,
 * see {@link PollingWait} for usage example.
 */
public abstract class RunnableAssert {

    private final String description;

    protected RunnableAssert(String description) {
        this.description = description;
    }

    /**
     * This method might be executed multiple times
     * by {@link PollingWait#until}, if it throws any
     * {@link Error} or {@link Exception}.
     */
    public abstract void run() throws Exception;

    @Override
    public String toString() {
        return "RunnableAssert(" + description + ")";
    }
}
