package com.googlecode.junittoolbox;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.googlecode.junittoolbox.PropertyContainer.getPropertyContainer;
import static org.junit.Assert.assertTrue;

@RunWith(ParallelRunner.class)
public class ParallelRunnerByClassesTest {

    private static long startTime;

    @BeforeClass
    public static void init() {
        getPropertyContainer().setParallelType("classes");
        startTime = System.currentTimeMillis();
    }

    @Test
    public void test_one_run_not_parallel() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void test_tw_run_not_parallel() throws InterruptedException {
        Thread.sleep(1000);
    }

    @Test
    public void test_three_run_not_parallel() throws InterruptedException {
        Thread.sleep(1000);
    }

    @AfterClass
    public static void checkTime() {
        final int testDuration = (int) ((System.currentTimeMillis() - startTime) / 1000);
        assertTrue(testDuration == 3);
    }

}
