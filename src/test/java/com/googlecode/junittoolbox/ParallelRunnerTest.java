package com.googlecode.junittoolbox;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeThat;
import static org.junit.Assume.assumeTrue;

public class ParallelRunnerTest {

    @RunWith(ParallelRunner.class)
    public static class Example {
        private static volatile CountDownLatch latch;
        static volatile Thread thread1;
        static volatile Thread thread2;

        @BeforeClass
        public static void init() {
            latch = new CountDownLatch(2);
        }

        @Test
        public void test1() throws InterruptedException {
            latch.countDown();
            assertTrue(latch.await(3, TimeUnit.SECONDS));
            thread1 = Thread.currentThread();
        }

        @Test
        public void test2() throws InterruptedException {
            latch.countDown();
            assertTrue(latch.await(3, TimeUnit.SECONDS));
            thread2 = Thread.currentThread();
        }
    }

    @Test
    public void test() {
        Result result = JUnitCore.runClasses(Example.class);
        assertTrue(result.wasSuccessful());
        assertEquals(2, result.getRunCount());
        assertNotNull(Example.thread1);
        assertNotNull(Example.thread2);
        assertNotSame(Example.thread1, Example.thread2);
    }

    @RunWith(ParallelRunner.class)
    public static class Example_with_theory_method {
        private static volatile CountDownLatch latch;
        private static volatile Thread[] threads;

        @BeforeClass
        public static void init() {
            latch = new CountDownLatch(2);
            threads = new Thread[2];
        }

        @DataPoints
        public static int[] TEST_DATA = { 0, 1 };

        @Theory
        public void test(int i) throws Exception {
            latch.countDown();
            assertTrue(latch.await(3, TimeUnit.SECONDS));
            threads[i] = Thread.currentThread();
        }
    }

    @Test
    public void test_with_theory_method() {
        Result result = JUnitCore.runClasses(Example_with_theory_method.class);
        assertTrue(result.wasSuccessful());
        assertEquals(1, result.getRunCount());
        assertNotNull(Example_with_theory_method.threads[0]);
        assertNotNull(Example_with_theory_method.threads[1]);
        assertNotSame(Example_with_theory_method.threads[0], Example_with_theory_method.threads[1]);
    }

    @RunWith(ParallelRunner.class)
    public static class Example_with_failing_theory_method {
        @DataPoints
        public static final int[] SOME_INTS = new int[] { 1, 2, 3 };

        @DataPoints
        public static final String[] SOME_STRINGS = new String[] { "foo", "bar", "xyz" };

        @Theory
        public void theory(int i, String s) throws Throwable {
            if (i == 2 && "bar".equals(s)) {
                fail("test");
            }
        }
    }

    @Test
    public void test_with_failing_theory_method() {
        Result result = JUnitCore.runClasses(Example_with_failing_theory_method.class);
        assertFalse(result.wasSuccessful());
        assertEquals(1, result.getRunCount());
        assertEquals(1, result.getFailureCount());
        Throwable failure = result.getFailures().get(0).getException();
        assertEquals(ParameterizedAssertionError.class, failure.getClass());
        assertEquals("theory(\"2\" <from SOME_INTS[1]>, \"bar\" <from SOME_STRINGS[1]>)", failure.getMessage());
        Throwable failureCause = failure.getCause();
        assertEquals(AssertionError.class, failureCause.getClass());
        assertEquals("test", failureCause.getMessage());
    }

    @RunWith(ParallelRunner.class)
    public static class Example_with_IOException_throwing_theory_method {
        @DataPoints
        public static final int[] SOME_INTS = new int[] { 1, 2, 3 };

        @DataPoints
        public static final String[] SOME_STRINGS = new String[] { "foo", "bar", "xyz" };

        @Theory
        public void theory(int i, String s) throws Exception {
            if (i == 2 && "bar".equals(s)) {
                throw new IOException("test");
            }
        }
    }

    @Test
    public void test_with_IOException_throwing_theory_method() {
        Result result = JUnitCore.runClasses(Example_with_IOException_throwing_theory_method.class);
        assertFalse(result.wasSuccessful());
        assertEquals(1, result.getRunCount());
        assertEquals(1, result.getFailureCount());
        Throwable failure = result.getFailures().get(0).getException();
        assertEquals(ParameterizedAssertionError.class, failure.getClass());
        assertEquals("theory(\"2\" <from SOME_INTS[1]>, \"bar\" <from SOME_STRINGS[1]>)", failure.getMessage());
        Throwable failureCause = failure.getCause();
        assertEquals(IOException.class, failureCause.getClass());
        assertEquals("test", failureCause.getMessage());
    }

    @RunWith(ParallelRunner.class)
    public static class Example_with_assume {

        @Test
        public void will_pass() throws Throwable {
            assertTrue(true);
        }

        @Test @Ignore
        public void will_be_ignored() throws Throwable {
            assumeTrue(true);
        }

        @Test
        public void will_be_ignored_too() throws Throwable {
            assumeTrue(false);
        }

        @Test
        public void will_fail() throws Throwable {
            assertTrue(false);
        }
    }

    @Test
    public void test_with_assume() {
        Result result = JUnitCore.runClasses(Example_with_assume.class);
        assertEquals(3, result.getRunCount());
        assertEquals(1, result.getIgnoreCount());
        assertEquals(1, result.getFailureCount());
    }

    // TODO: @RunWith(ParallelRunner.class)
    @RunWith(Theories.class)
    public static class Example_with_theory_method_and_assume {
        @DataPoints
        public static final String[] SOME_STRINGS = new String[] { "foo", null, "bar" };

        @Theory
        public void correct_theory(String s) throws Throwable {
            assumeThat(s, is(not(nullValue())));
            assertThat(s.length(), is(equalTo(3)));
        }

        @Theory
        public void wrong_theory(String s) throws Throwable {
            assumeThat(s, is(not(nullValue())));
            assertThat(s, startsWith("f"));
        }
    }

    @Test
    @Ignore("Strange: Already fails when Example_with_theory_method_and_assume is annotated with @RunWith(Theories.class)")
    public void test_with_theory_method_and_assume() {
        Result result = JUnitCore.runClasses(Example.class);
        assertEquals(2, result.getRunCount());
        assertEquals(1, result.getFailureCount());
    }
}
