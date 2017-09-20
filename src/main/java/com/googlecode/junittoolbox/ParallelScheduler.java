package com.googlecode.junittoolbox;

import com.googlecode.junittoolbox.util.MultiException;
import org.junit.runners.model.RunnerScheduler;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;

import static java.util.concurrent.ForkJoinTask.inForkJoinPool;

/**
 * Encapsulates the singleton {@link ForkJoinPool} used
 * by {@link ParallelRunner} and {@link ParallelSuite}
 * to execute test classes and test methods concurrently.
 */
class ParallelScheduler implements RunnerScheduler {

    private static final ForkJoinPool FORK_JOIN_POOL = setUpForkJoinPool();

    private static ForkJoinPool setUpForkJoinPool() {
        int numThreads;
        try {
            String configuredNumThreads = System.getProperty("maxParallelTestThreads");
            numThreads = Math.max(2, Integer.parseInt(configuredNumThreads));
        } catch (Exception ignored) {
            Runtime runtime = Runtime.getRuntime();
            numThreads = Math.max(2, runtime.availableProcessors());
        }
        ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            @Override
            public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                if (pool.getPoolSize() > pool.getParallelism()) {
                    return null;
                } else {
                    ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    thread.setName("JUnit-" + thread.getName());
                    return thread;
                }
            }
        };
        return new ForkJoinPool(numThreads, threadFactory, null, false);
    }

    private final Deque<ForkJoinTask<?>> _asyncTasks = new LinkedList<>();
    private Runnable _lastScheduledChild;

    @Override
    public void schedule(Runnable childStatement) {
        if (_lastScheduledChild != null) {
            // Execute previously scheduled child asynchronously ...
            if (inForkJoinPool()) {
                _asyncTasks.addFirst(ForkJoinTask.adapt(_lastScheduledChild).fork());
            } else {
                _asyncTasks.addFirst(FORK_JOIN_POOL.submit(_lastScheduledChild));
            }
        }
        // Note: We don't schedule the childStatement immediately here,
        // but remember it, so that we can synchronously execute the
        // last scheduled child in the finished method() -- this way,
        // the current thread does not immediately call join() in the
        // finished() method, which might block it ...
        _lastScheduledChild = childStatement;
    }

    @Override
    public void finished() {
        MultiException me = new MultiException();
        if (_lastScheduledChild != null) {
            if (inForkJoinPool()) {
                // Execute the last scheduled child in the current thread ...
                try { _lastScheduledChild.run(); } catch (Throwable t) { me.add(t); }
            } else {
                // Submit the last scheduled child to the ForkJoinPool too,
                // because all tests should run in the worker threads ...
                _asyncTasks.addFirst(FORK_JOIN_POOL.submit(_lastScheduledChild));
            }
            // Make sure all asynchronously executed children are done, before we return ...
            for (ForkJoinTask<?> task : _asyncTasks) {
                // Note: Because we have added all tasks via addFirst into _asyncTasks,
                // task.join() is able to steal tasks from other worker threads,
                // if there are tasks, which have not been started yet ...
                // from other worker threads ...
                try { task.join(); } catch (Throwable t) { me.add(t); }
            }
            me.throwIfNotEmpty();
        }
    }
}
