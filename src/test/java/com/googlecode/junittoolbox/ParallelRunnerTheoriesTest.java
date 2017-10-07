package com.googlecode.junittoolbox;

import org.junit.ClassRule;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;

import jsr166y.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Math.max;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(ParallelRunner.class)
public class ParallelRunnerTheoriesTest {

    private static final long MAX_PARALLEL_THREADS = 2L;

    // sets the maximum number of parallel test threads of the fork join pool to 2 ...
    private static final TestRule SET_MAX_PARALLEL_TEST_THREADS = new TestRule() {
        @Override
        public Statement apply(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    String maxParallelTestThreadsProperty = "maxParallelTestThreads";
                    String oldMaxParallelTestThreadsProperty = System.getProperty(maxParallelTestThreadsProperty);
                    ForkJoinPool oldForkJoinPool = ParallelScheduler.forkJoinPool;
                    System.setProperty(maxParallelTestThreadsProperty, String.valueOf(MAX_PARALLEL_THREADS));
                    try {
                        ParallelScheduler.forkJoinPool = ParallelScheduler.setUpForkJoinPool();
                        base.evaluate();
                    } finally {
                        ParallelScheduler.forkJoinPool = oldForkJoinPool;
                        if (oldMaxParallelTestThreadsProperty == null) {
                            System.clearProperty(maxParallelTestThreadsProperty);
                        } else {
                            System.setProperty(maxParallelTestThreadsProperty, oldMaxParallelTestThreadsProperty);
                        }
                    }
                }
            };
        }
    };

    // observes the pool size of the fork join pool and fails if it exceeds the maximum value of 2 ...
    private static final TestRule THREAD_COUNT_OBSERVER = new TestRule() {
        @Override
        public Statement apply(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    final AtomicLong maxPoolSize = new AtomicLong();
                    final AtomicBoolean observe = new AtomicBoolean(true);
                    Thread observer = new Thread() {
                        @Override
                        public void run() {
                            while (observe.get()) {
                                try {
                                    long poolSize = ParallelScheduler.forkJoinPool.getPoolSize();
                                    maxPoolSize.set(max(maxPoolSize.get(), poolSize));
                                    Thread.sleep(1L);
                                } catch (InterruptedException ignored) {
                                    observe.set(true);
                                }
                            }
                        }
                    };
                    observer.start();
                    try {
                        base.evaluate();
                    } finally {
                        observe.set(false);
                        observer.join();
                    }
                    assertThat(maxPoolSize.get(), lessThanOrEqualTo(MAX_PARALLEL_THREADS));
                }
            };
        }
    };

    @ClassRule
    public static final RuleChain RULE_CHAIN = RuleChain.outerRule(SET_MAX_PARALLEL_TEST_THREADS).around(THREAD_COUNT_OBSERVER);

    @DataPoints
    public static final int[] DATA_POINTS = new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };

    // the bug can be reproduced with multiple tests and multiple parameters ...
    @Theory public void test1(int a, int b, int c) throws Throwable { Thread.sleep(1); }
    @Theory public void test2(int a, int b, int c) throws Throwable { Thread.sleep(1); }
    @Theory public void test3(int a, int b, int c) throws Throwable { Thread.sleep(1); }
    @Theory public void test4(int a, int b, int c) throws Throwable { Thread.sleep(1); }
    @Theory public void test5(int a, int b, int c) throws Throwable { Thread.sleep(1); }
}
