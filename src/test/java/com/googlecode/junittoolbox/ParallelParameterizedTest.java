package com.googlecode.junittoolbox;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class ParallelParameterizedTest {

    @RunWith(ParallelParameterized.class)
    public static class Example {
        private static volatile CountDownLatch latch;
        private static volatile Thread[] threads;

        @BeforeClass
        public static void init() {
            latch = new CountDownLatch(2);
            threads = new Thread[2];
        }

        @Parameters
        public static Iterable<Object[]> testData() {
            return asList(
                new Object[]{ 0 },
                new Object[]{ 1 }
            );
        }

        @Parameter(0)
        public int testParameter;

        @Test
        public void test() throws InterruptedException {
            latch.countDown();
            assertTrue(latch.await(3, TimeUnit.SECONDS));
            threads[testParameter] = Thread.currentThread();
        }
    }

    @Test
    public void test() {
        Result result = JUnitCore.runClasses(Example.class);
        assertTrue(result.wasSuccessful());
        assertEquals(2, result.getRunCount());
        assertNotNull(Example.threads[0]);
        assertNotNull(Example.threads[1]);
        assertNotSame(Example.threads[0], Example.threads[1]);
    }
}
