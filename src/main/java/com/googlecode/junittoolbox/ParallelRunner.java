package com.googlecode.junittoolbox;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * A JUnit 4 runner which executes the test methods of a test class concurrently.
 */
public class ParallelRunner extends BlockJUnit4ClassRunner {

    public ParallelRunner(final Class<?> klass) throws InitializationError {
        super(klass);
        setScheduler(new ParallelScheduler());
    }
}
