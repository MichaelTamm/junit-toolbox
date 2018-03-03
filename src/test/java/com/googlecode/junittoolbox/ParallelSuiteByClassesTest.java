package com.googlecode.junittoolbox;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.Suite.SuiteClasses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParallelSuiteByClassesTest {

    @RunWith(ParallelRunner.class)
    public static class Test1 {

        @Test
        public void testOne() throws InterruptedException {
            Thread.sleep(1000);
        }

        @Test
        public void testTwo() throws InterruptedException {
            Thread.sleep(1000);
        }

        @Test
        public void testThree() throws InterruptedException {
            Thread.sleep(1000);
        }
    }

    @RunWith(ParallelRunner.class)
    public static class Test2 {

        @Test
        public void testOne() throws InterruptedException {
            Thread.sleep(1000);
        }

        @Test
        public void testTwo() throws InterruptedException {
            Thread.sleep(1000);
        }

        @Test
        public void testThree() throws InterruptedException {
            Thread.sleep(1000);
        }
    }

    @RunWith(ParallelSuite.class)
    @SuiteClasses({Test1.class, Test2.class})
    public static class ByMethod { }

    @RunWith(ParallelSuite.class)
    @SuiteClasses({Test1.class, Test2.class})
    @SuiteConfiguration(parallel = "classes")
    public static class ByClasses { }

    @Test
    public void testByMethod() {
        final Result result = JUnitCore.runClasses(ByMethod.class);
        assertTrue(result.wasSuccessful());
        assertEquals(6, result.getRunCount());
        assertTrue((result.getRunTime() / 1000) <= 3);
    }

    @Test
    public void testByClasses() {
        final Result result = JUnitCore.runClasses(ByClasses.class);
        assertTrue(result.wasSuccessful());
        assertEquals(6, result.getRunCount());
        assertTrue((result.getRunTime() / 1000) >= 3);
    }
}
