package com.googlecode.junittoolbox;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * An extension of the JUnit {@link BlockJUnit4ClassRunner} &ndash; the default
 * runner for JUnit 4 test classes &ndash; which executes the test methods of
 * the test class concurrently, if you annotate it like this:<pre>
 *     &#64;RunWith(ParallelRunner.class)
 *     public class FooTest {
 *         ...
 *     }
 * </pre>
 * You can specify the maximum number of parallel test
 * threads using the system property <code>maxParallelTestThreads</code>.
 * If this system property is not specified, the maximum
 * number of test threads will be the number of
 * {@link Runtime#availableProcessors() available processors.}
 */
public class ParallelRunner extends BlockJUnit4ClassRunner {

    public ParallelRunner(final Class<?> klass) throws InitializationError {
        super(klass);
        setScheduler(new ParallelScheduler());
    }
}
