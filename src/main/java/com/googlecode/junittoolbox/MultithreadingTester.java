package com.googlecode.junittoolbox;

import com.googlecode.junittoolbox.util.MultiException;

import javax.annotation.Nonnull;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
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
        checkArrayItemsNotNull(moreRunnableAsserts, "moreRunnableAsserts");
        this.runnableAsserts.add(checkNotNull(runnableAssert, "runnableAssert"));
        Collections.addAll(this.runnableAsserts, moreRunnableAsserts);
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
        checkArrayItemsNotNull(moreRunnables, "moreRunnables");
        this.runnableAsserts.add(convertToRunnableAssert(checkNotNull(runnable, "runnable")));
        for (Runnable aRunnable: moreRunnables) {
            this.runnableAsserts.add(convertToRunnableAssert(aRunnable));
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
        checkArrayItemsNotNull(moreCallables, "moreCallables");
        this.runnableAsserts.add(convertToRunnableAssert(checkNotNull(callable, "callable")));
        for (Callable<?> aCallable: moreCallables) {
            this.runnableAsserts.add(convertToRunnableAssert(aCallable));
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

    private void checkArrayItemsNotNull(@Nonnull Object[] items, @Nonnull String label) {
        for (int i = 0; i < items.length; ++i) {
            checkNotNull(items[i], label + "[" + i  + "]");
        }
    }

    private Thread monitorThread;
    private Thread[] workerThreads;
    private Set<Long> idsOfDeadlockedThreads = new CopyOnWriteArraySet<Long>();

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
            throw new IllegalStateException("You added more RunnableAsserts (" + runnableAsserts.size() + ") than the number of threads (" + numThreads + ") configured for this MultithreadingTester");
        }
        if (runnableAsserts.isEmpty()) {
            throw new IllegalStateException("You must add at least 1 RunnableAssert before you can call run()");
        }
        final MultiException me = new MultiException();
        startMonitorThread(me);
        try {
            startWorkerThreads(me);
            joinWorkerThreads();
        } finally {
            stopMonitorThread();
        }
        me.throwIfNotEmpty();
    }

    private void startMonitorThread(final MultiException me) {
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        final Set<Long> knownDeadlockedThreadIds = asSet(threadMXBean.findDeadlockedThreads());
        monitorThread = new Thread("MultithreadingTester-monitor") {
            @Override
            public void run() {
                try {
                    while (!interrupted()) {
                        long[] threadIds = threadMXBean.findDeadlockedThreads();
                        if (threadIds != null) {
                            Set<Long> temp = asSet(threadIds);
                            temp.removeAll(knownDeadlockedThreadIds);
                            if (!temp.isEmpty()) {
                                idsOfDeadlockedThreads.addAll(temp);
                                StringBuilder sb = new StringBuilder();
                                sb.append("Detected ").append(threadIds.length).append(" deadlocked threads:");
                                for (ThreadInfo threadInfo : threadMXBean.getThreadInfo(threadIds, true, true)) {
                                    sb.append('\n').append(threadInfo);
                                }
                                me.add(new RuntimeException(sb.toString()));
                                return;
                            }
                        }
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException expected) {
                } catch (Throwable unexpected) {
                    me.add(unexpected);
                }
            }
        };
        monitorThread.setPriority(Thread.MAX_PRIORITY);
        monitorThread.start();
    }

    private void startWorkerThreads(final MultiException me) {
        workerThreads = new Thread[numThreads];
        Iterator<RunnableAssert> i = runnableAsserts.iterator();
        final CountDownLatch latch = new CountDownLatch(numThreads);
        for (int j = 0; j < numThreads; ++j) {
            if (!i.hasNext()) {
                i = runnableAsserts.iterator();
            }
            final RunnableAssert runnableAssert = i.next();
            Thread workerThread = new Thread("MultithreadingTester-worker-" + (j + 1)) {
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
            workerThread.start();
            workerThreads[j] = workerThread;
        }
    }

    private void joinWorkerThreads() {
        boolean foundAliveWorkerThread;
        do {
            foundAliveWorkerThread = false;
            for (int i = 0; i < numThreads; ++i) {
                try {
                    Thread workerThread = workerThreads[i];
                    workerThread.join(100);
                    if (workerThread.isAlive() && !idsOfDeadlockedThreads.contains(workerThread.getId())) {
                        foundAliveWorkerThread = true;
                    }
                } catch (InterruptedException e) {
                    for (int j = 0; j < numThreads; ++j) {
                        workerThreads[j].interrupt();
                    }
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Got interrupted", e);
                }
            }
        } while (foundAliveWorkerThread && monitorThread.isAlive());
    }

    private void stopMonitorThread() {
        monitorThread.interrupt();
        try {
            monitorThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Got interrupted", e);
        }
    }

    private Set<Long> asSet(long[] array) {
        Set<Long> set = new HashSet<Long>();
        if (array != null) {
            for (long x : array) {
                set.add(x);
            }
        }
        return set;
    }
}
