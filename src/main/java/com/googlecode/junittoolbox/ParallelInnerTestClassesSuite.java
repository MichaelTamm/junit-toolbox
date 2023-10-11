package com.googlecode.junittoolbox;

import org.junit.experimental.runners.Enclosed;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

/**
 * Runs all inner test classes of the class
 * annotated with <code>&#64;RunWith(ParallelInnerTestClassesSuite&#46;class)</code>.
 * It executes its children classes concurrently. You can specify the maximum number
 * of parallel test threads using the system property <code>maxParallelTestThreads</code>.
 * If this system property is not specified, the maximum number of test threads
 * will be the number of {@link Runtime#availableProcessors() available processors.}
 * In contrast to the {@link Enclosed} runner provided by
 * <a href="http://junit.org/" target="_blank">JUnit</a>,
 * it detects if an inner class is actually a test class
 * and ignores all other inner classes.
 * Example:<pre>
 *     &#64;RunWith(ParallelInnerTestClassesSuite.class)
 *     public class LoginBeanTests {
 *
 *         public static class UnitTests {
 *             &#64;Test
 *             public void test1() { ... }
 *         }
 *
 *         &#64;Configuration
 *         public static class IntegrationTestsConfig { ... }
 *
 *         &#64;RunWith(SpringJUnit4ClassRunner.class)
 *         &#64;ContextConfiguration(classes = IntegrationTestsConfig.class)
 *         public static class IntegrationTests {
 *             &#64;Test
 *             public void test2() { ... }
 *         }
 *     }
 * </pre>
 */
public class ParallelInnerTestClassesSuite extends InnerTestClassesSuite {
    public ParallelInnerTestClassesSuite(Class<?> klass, RunnerBuilder runnerBuilder) throws InitializationError {
        super(klass, runnerBuilder);
        setScheduler(new ParallelScheduler());
    }
}
