package com.googlecode.junittoolbox;

import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * An extension of the {@link Suite} runner, which executes its children classes concurrently.
 * You can either explicitly list the children classes using the
 * <code>{@link org.junit.runners.Suite.SuiteClasses @SuiteClasses}</code> annotation
 * provided by JUnit itself, like this:<pre>
 *     &#64;RunWith(ParallelSuite.class)
 *     &#64;SuiteClasses({
 *         LoginFrontendTest.class,
 *         FillOutFormFrontendTest.class,
 *         ...
 *     })
 *     public class AllFrontendTests {}
 * </pre>
 * Or you can specify a wildcard pattern using the <code>{@link com.googlecode.junittoolbox.SuiteClasses @SuiteClasses}</code> annotation:<pre>
 *     &#64;RunWith(ParallelSuite.class)
 *     &#64;SuiteClasses("&#42;&#42;/&#42;FrontendTest.class")
 *     public class AllFrontendTests {}
 * </pre>
 * You can specify the maximum number of parallel test
 * threads using the system property "maxParallelTestThreads".
 * If this system property is not specified, the maximum
 * number of test threads will be the number of
 * {@link Runtime#availableProcessors() available processors.}
 */
public class ParallelSuite extends Suite {

    public ParallelSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        setScheduler(new ParallelScheduler());
    }
}
