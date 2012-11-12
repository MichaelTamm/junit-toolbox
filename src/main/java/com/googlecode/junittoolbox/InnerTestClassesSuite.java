package com.googlecode.junittoolbox;

import org.junit.experimental.runners.Enclosed;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs all inner test classes of the class
 * annotated with <code>&#64;RunWith(InnerTestClassesSuite&#46;class)</code>.
 * In contrast to the {@link Enclosed} runner provided by
 * <a href="https://github.com/KentBeck/junit">JUnit</a>,
 * it detects if an inner class is actually a test class
 * and ignores all other inner classes.
 * Example:<pre>
 *     &#64;RunWith(InnerTestClassesSuite.class)
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
public class InnerTestClassesSuite extends Suite {

    private static List<Runner> getRunnersForInnerTestClasses(Class<?> klass, RunnerBuilder runnerBuilder) throws InitializationError {
        Class<?>[] innerClasses = klass.getClasses();
        final List<Runner> runners = new ArrayList<Runner>();
        for (Class<?> innerClass : innerClasses) {
            try {
                Runner runner = runnerBuilder.runnerForClass(innerClass);
                if (runner instanceof ErrorReportingRunner) {
                    // runnerBuilder.runnerForClass(innerClass) failed,
                    // inner class is not a test class and therefore ignored
                } else {
                    runners.add(runner);
                }
            } catch (Throwable ignored) {
                // runnerBuilder.runnerForClass(innerClass) failed,
                // inner class is not a test class and therefore ignored
            }
        }
        return runners;
    }

    public InnerTestClassesSuite(Class<?> klass, RunnerBuilder runnerBuilder) throws InitializationError {
        super(klass, getRunnersForInnerTestClasses(klass, runnerBuilder));
    }
}
