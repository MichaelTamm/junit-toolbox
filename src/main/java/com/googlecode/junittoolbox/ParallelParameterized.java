package com.googlecode.junittoolbox;

import org.junit.runners.Parameterized;

/**
 * An extension of the JUnit {@link Parameterized}
 * runner, which executes the tests for each parameter set
 * concurrently.
 * <p>You can specify the maximum number of parallel test
 * threads using the system property <code>maxParallelTestThreads</code>.
 * If this system property is not specified, the maximum
 * number of test threads will be the number of
 * {@link Runtime#availableProcessors() available processors}.
 */
public class ParallelParameterized extends Parameterized {

    public ParallelParameterized(Class<?> klass) throws Throwable {
        super(klass);
        setScheduler(new ParallelScheduler());
    }
}
