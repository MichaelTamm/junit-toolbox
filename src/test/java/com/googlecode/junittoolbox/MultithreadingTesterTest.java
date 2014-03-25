package com.googlecode.junittoolbox;

import org.junit.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MultithreadingTesterTest {

    @Test(timeout = 5000)
    public void test() {
        RunnableAssert ra = new RunnableAssert("foo") {
            @Override
            public void run() {
                fail("foo");
            }
        };
        boolean success = false;
        try {
            new MultithreadingTester().add(ra).run();
            success = true;
        } catch (Throwable expected) {}
        assertFalse(success);
    }

    @Test(timeout = 5000)
    public void test_with_long_running_worker() {
        new MultithreadingTester().numThreads(2).numRoundsPerThread(1).add(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Thread.sleep(1000);
                return null;
            }
        }).run();
    }

    @Test(timeout = 5000)
    public void test_with_one_RunnableAssert() {
        CountingRunnableAssert ra1 = new CountingRunnableAssert();
        new MultithreadingTester().numThreads(11)
                                 .numRoundsPerThread(13)
                                 .add(ra1)
                                 .run();
        assertThat(ra1.count.get(), is(11 * 13));
    }

    @Test(timeout = 5000)
    public void test_with_two_RunnableAsserts() {
        CountingRunnableAssert ra1 = new CountingRunnableAssert();
        CountingRunnableAssert ra2 = new CountingRunnableAssert();
        new MultithreadingTester().numThreads(3)
                                 .numRoundsPerThread(1)
                                 .add(ra1)
                                 .add(ra2)
                                 .run();
        assertThat(ra1.count.get(), is(2));
        assertThat(ra2.count.get(), is(1));
    }

    @Test(timeout = 5000)
    public void test_with_more_RunnableAsserts_than_threads() {
        RunnableAssert ra = new CountingRunnableAssert();
        MultithreadingTester mt = new MultithreadingTester().numThreads(2)
                                 .add(ra)
                                 .add(ra)
                                 .add(ra);
        try {
            mt.run();
            fail("IllegalStateException expected");
        } catch (IllegalStateException expected) {}
    }

    @Test(timeout = 5000)
    public void test_that_deadlock_is_detected() {
        try {
            final Object lock1 = new Object();
            final CountDownLatch latch1 = new CountDownLatch(1);
            final Object lock2 = new Object();
            final CountDownLatch latch2 = new CountDownLatch(1);
            new MultithreadingTester().numThreads(2).numRoundsPerThread(1).add(
                new RunnableAssert("synchronize on lock1 and lock2") {
                    @Override
                    public void run() throws Exception {
                        synchronized (lock1) {
                            latch2.countDown();
                            latch1.await();
                            synchronized (lock2) {
                                fail("Reached unreachable statement.");
                            }
                        }
                    }
                },
                new RunnableAssert("synchronize on lock2 and lock1") {
                    @Override
                    public void run() throws Exception {
                        synchronized (lock2) {
                            latch1.countDown();
                            latch2.await();
                            synchronized (lock1) {
                                fail("Reached unreachable statement.");
                            }
                        }
                    }
                }
            ).run();
            fail("RuntimeException expected");
        } catch (RuntimeException expected) {
            assertThat(expected.getMessage(), allOf(
                containsString("Detected 2 deadlocked threads:\n"),
                containsString("MultithreadingTesterTest.java:92"),
                containsString("MultithreadingTesterTest.java:104")
            ));
        }
    }

    private class CountingRunnableAssert extends RunnableAssert {
        protected AtomicInteger count = new AtomicInteger(0);

        protected CountingRunnableAssert() {
            super("CountingRunnableAssert");
        }

        @Override
        public void run() {
            count.incrementAndGet();
        }
    }
}
