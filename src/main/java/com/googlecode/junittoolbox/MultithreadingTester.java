package com.googlecode.junittoolbox;

import com.googlecode.junittoolbox.util.MultiException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import static org.mockito.internal.util.Checks.checkItemsNotNull;
import static org.mockito.internal.util.Checks.checkNotNull;

/**
 * Runs one ore more {@link RunnableAssert}s
 * concurrently in multiple threads several times.
 * Use this class for testing the robustness
 * of thread-safe code like this:<pre>
 *     new MultithreadingTester().add(...).run();
 * </pre>
 * If an <code>Exception</code> or <code>Error</code> is
 * caught in any of the threads, the {@link #run run()} method
 * (and therefore the test which calls the <code>run()</code>
 * methods) will fail.
 * Per default 100 threads are used whereby each thread
 * executes its {@link RunnableAssert} 1000 times.
 * You can change these settings like this:<pre>
 *     new MultithreadingTester().numThreads(...)
 *                               .numRoundsPerThread(...)
 *                               .add(...)
 *                               .run();
 * </pre>
 */
public class MultithreadingTester {

    private int numThreads = 100;
    private int roundsPerThreads = 1000;
    private List<RunnableAssert> runnableAsserts = new ArrayList<RunnableAssert>();

    /**
     * Sets the number of threads used by {@link #run},
     * default is <code>100</code>,
     * returns <code>this</code> to allow method chaining.
     */
    public MultithreadingTester numThreads(int numThreads) {
        if (numThreads <= 1) {
            throw new IllegalArgumentException("Invalid numThreads parameter: " + numThreads + " -- must be greater than 1");
        }
        this.numThreads = numThreads;
        return this;
    }

    /**
     * Sets the number of rounds per thread,
     * default is <code>1000</code>,
     * returns <code>this</code> to allow method chaining.
     */
    public MultithreadingTester numRoundsPerThread(int roundsPerThreads) {
        if (roundsPerThreads <= 0) {
            throw new IllegalArgumentException("Invalid roundsPerThreads parameter: " + roundsPerThreads + " -- must be greater than 0");
        }
        this.roundsPerThreads = roundsPerThreads;
        return this;
    }

    /**
     * Adds the given {@link RunnableAssert}s to this <code>MultithreadingTester</code>,
     * returns <code>this</code> to allow method chaining.
     */
    public MultithreadingTester add(@Nonnull RunnableAssert runnableAssert, RunnableAssert... moreRunnableAsserts) {
        this.runnableAsserts.add(checkNotNull(runnableAssert, "runnableAssert"));
        for (int i = 0; i < moreRunnableAsserts.length; ++i) {
            this.runnableAsserts.add(checkNotNull(moreRunnableAsserts[i], "moreRunnableAsserts[" + i  + "]"));
        }
        return this;
    }

    /**
     * Adds the given {@link RunnableAssert}s to this <code>MultithreadingTester</code>,
     * returns <code>this</code> to allow method chaining.
     */
    public MultithreadingTester add(@Nonnull Collection<RunnableAssert> runnableAsserts) {
        checkItemsNotNull(runnableAsserts, "runnableAsserts");
        this.runnableAsserts.addAll(runnableAsserts);
        return this;
    }

    /**
     * Converts the given {@link Runnable}s into {@link RunnableAssert}s
     * and adds them to this <code>MultithreadingTester</code>,
     * returns <code>this</code> to allow method chaining.
     */
    public MultithreadingTester add(@Nonnull Runnable runnable, Runnable... moreRunnables) {
        this.runnableAsserts.add(convertToRunnableAssert(checkNotNull(runnable, "runnable")));
        for (int i = 0; i < moreRunnables.length; ++i) {
            this.runnableAsserts.add(convertToRunnableAssert(checkNotNull(moreRunnables[i], "moreRunnables[" + i  + "]")));
        }
        return this;
    }

    private static RunnableAssert convertToRunnableAssert(final @Nonnull Runnable runnable) {
        return new RunnableAssert(runnable.toString()) {
            @Override
            public void run() {
                runnable.run();
            }
        };
    }

    /**
     * Converts the given {@link Callable}s into {@link RunnableAssert}s
     * and adds them to this <code>MultithreadingTester</code>,
     * returns <code>this</code> to allow method chaining.
     */
    public MultithreadingTester add(@Nonnull Callable<?> callable, Callable<?>... moreCallables) {
        this.runnableAsserts.add(convertToRunnableAssert(checkNotNull(callable, "callable")));
        for (int i = 0; i < moreCallables.length; ++i) {
            this.runnableAsserts.add(convertToRunnableAssert(checkNotNull(moreCallables[i], "moreCallables[" + i  + "]")));
        }
        return this;
    }

    private static RunnableAssert convertToRunnableAssert(final @Nonnull Callable<?> callable) {
        return new RunnableAssert(callable.toString()) {
            @Override
            public void run() throws Exception {
                callable.call();
            }
        };
    }
    /**
     * Starts multiple threads, which execute the added {@link RunnableAssert}s
     * several times. This method blocks until all started threads are finished.
     * If an <code>Exception</code> or <code>Error</code> is caught in any
     * of the started threads, this method will also throw an <code>Exception</code>
     * or <code>Error</code>.
     *
     * @see #numThreads(int)
     * @see #numRoundsPerThread(int)
     */
    public void run() {
        if (runnableAsserts.size() > numThreads) {
            throw new IllegalStateException("You added more runnable asserts (" + runnableAsserts.size() + ") than the number of threads (" + numThreads + ") used by this MultithreadingTester");
        }
        if (runnableAsserts.isEmpty()) {
            throw new IllegalStateException("You must add at least 1 runnable assert before you can call run()");
        }
        Thread[] threads = new Thread[numThreads];
        Iterator<RunnableAssert> i = runnableAsserts.iterator();
        final CountDownLatch latch = new CountDownLatch(numThreads);
        final MultiException me = new MultiException();
        for (int j = 0; j < numThreads; ++j) {
            if (!i.hasNext()) {
                i = runnableAsserts.iterator();
            }
            final RunnableAssert runnableAssert = i.next();
            threads[j] = new Thread("MultithreadingTester-thread-" + (j + 1)) {
                @Override
                public void run() {
                    try {
                        latch.countDown();
                        latch.await();
                        for (int i = 0; i < roundsPerThreads; ++i) {
                            runnableAssert.run();
                        }
                    } catch (Throwable t) {
                        me.add(t);
                    }
                }
            };
            threads[j].start();
        }
        for (int j = 0; j < numThreads; ++j) {
            try {
                threads[j].join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Got interrupted", e);
            }
        }
        me.throwIfNotEmpty();
    }
}
