package com.googlecode.junittoolbox;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

public class MultithreadingTesterTest {

    @Test
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

    @Test
    public void test_with_one_RunnableAssert() {
        CountingRunnableAssert ra1 = new CountingRunnableAssert();
        new MultithreadingTester().numThreads(11)
                                 .numRoundsPerThread(13)
                                 .add(ra1)
                                 .run();
        assertThat(ra1.count.get(), is(11 * 13));
    }

    @Test
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

    @Test
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
