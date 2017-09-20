package com.googlecode.junittoolbox;

import com.googlecode.junittoolbox.ParallelRunnerTheoriesTest.ParallelRunnerWithTwoThreads;
import org.junit.ClassRule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.max;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(ParallelRunnerWithTwoThreads.class)
public class ParallelRunnerTheoriesTest {

    // work around to set the system property "maxParallelTestThreads" to "2" before it is read ...
    public static class ParallelRunnerWithTwoThreads extends ParallelRunner {

        private static final String MAX_PARALLEL_THREADS_PROPERTY = "maxParallelTestThreads";
        private static final int MAX_PARALLEL_THREADS = 2;
        private static String c_oldMaxParallelTestThreads;

        public ParallelRunnerWithTwoThreads(Class<?> clazz) throws InitializationError {
            super(setMaxParallelTestThreadsBeforeSuperConstructorCall(clazz));
            if (c_oldMaxParallelTestThreads == null) {
                System.clearProperty(MAX_PARALLEL_THREADS_PROPERTY);
            } else {
                System.setProperty(MAX_PARALLEL_THREADS_PROPERTY, c_oldMaxParallelTestThreads);
            }
        }

        private static Class<?> setMaxParallelTestThreadsBeforeSuperConstructorCall(Class<?> clazz) {
            c_oldMaxParallelTestThreads = System.getProperty(MAX_PARALLEL_THREADS_PROPERTY);
            System.setProperty(MAX_PARALLEL_THREADS_PROPERTY, Integer.toString(MAX_PARALLEL_THREADS));
            return clazz;
        }
    }

    // observes the worker count and fails if it exceeds the maximum value of 2 ...
    @ClassRule
    public static final TestRule THREAD_COUNT_OBSERVER = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            AtomicLong numberOfThreads = new AtomicLong();
            AtomicBoolean observe = new AtomicBoolean(true);
            Thread observer = new Thread(() -> {
                while (observe.get()) {
                    try {
                        long workerThreadCount = Thread.getAllStackTraces().keySet().stream().map(Thread::getName).filter(name -> name.contains("ForkJoinPool-")).count();
                        numberOfThreads.set(max(numberOfThreads.get(), workerThreadCount));
                        Thread.sleep(1L);
                    } catch (InterruptedException ignored) {
                        observe.set(true);
                    }
                }
            });
            observer.start();
            try {
                base.evaluate();
            } finally {
                observe.set(false);
                observer.join();
            }
            assertThat(numberOfThreads.get(), lessThanOrEqualTo(2L));
        }
    };

    @DataPoints
    public static final int[] DATA_POINTS = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    // the bug can be reproduced with multiple tests and multiple parameters ...
    @Theory public void test1(int a, int b, int c) throws Throwable { Thread.sleep(1); }
    @Theory public void test2(int a, int b, int c) throws Throwable { Thread.sleep(1); }
    @Theory public void test3(int a, int b, int c) throws Throwable { Thread.sleep(1); }
    @Theory public void test4(int a, int b, int c) throws Throwable { Thread.sleep(1); }
    @Theory public void test5(int a, int b, int c) throws Throwable { Thread.sleep(1); }
}
