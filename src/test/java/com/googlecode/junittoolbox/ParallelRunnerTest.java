package com.googlecode.junittoolbox;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

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
}
