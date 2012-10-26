package com.googlecode.junittoolbox;

import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * An extension of the {@link WildcardPatternSuite} runner, which executes
 * its children classes concurrently.You can specify the maximum number
 * of parallel test threads using the system property <code>maxParallelTestThreads</code>.
 * If this system property is not specified, the maximum number of test threads
 * will be the number of {@link Runtime#availableProcessors() available processors.}
 * You can either explicitly list the children classes using the
 * <code>{@link org.junit.runners.Suite.SuiteClasses @SuiteClasses}</code> annotation
 * provided by <a href="https://github.com/KentBeck/junit">JUnit</a> itself, like this:<pre>
 *     &#64;RunWith(ParallelSuite.class)
 *     &#64;SuiteClasses({
 *         LoginFrontendTest.class,
 *         FillOutFormFrontendTest.class,
 *         ...
 *     })
 *     public class AllFrontendTests {}
 * </pre>
 * Or you can specify a wildcard pattern using the <code>{@link com.googlecode.junittoolbox.SuiteClasses @SuiteClasses}</code> annotation
 * provided by <a href="http://junit-toolbox.googlecode.com/">junit-toolbox</a>:<pre>
 *     &#64;RunWith(ParallelSuite.class)
 *     &#64;SuiteClasses("&#42;&#42;/&#42;FrontendTest.class")
 *     public class AllFrontendTests {}
 * </pre>
 * Note: Even if a test class annotated with <code>@RunWith(ParallelSuite.class)</code>
 * has a child class, which is also annotated with <code>@RunWith(ParallelSuite.class)</code>
 * or with <code>@RunWith(ParallelRunner.class)</code>, there will never be
 * more test threads than specified (or the number of available processors), because
 * <code>ParallelSuite</code> and <code>{@link ParallelRunner}</code> share a singleton
 * fork join pool.
 */
public class ParallelSuite extends WildcardPatternSuite {

    public ParallelSuite(Class<?> klass, RunnerBuilder builder) throws InitializationError {
        super(klass, builder);
        setScheduler(new ParallelScheduler());
    }
}
