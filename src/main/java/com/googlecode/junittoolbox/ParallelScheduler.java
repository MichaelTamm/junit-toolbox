package com.googlecode.junittoolbox;

import jsr166y.ForkJoinPool;
import jsr166y.ForkJoinTask;
import jsr166y.ForkJoinWorkerThread;
import org.junit.runners.model.RunnerScheduler;

import java.util.Deque;
import java.util.LinkedList;

import static jsr166y.ForkJoinTask.inForkJoinPool;

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
            final String configuredNumThreads = System.getProperty("maxParallelTestThreads");
            numThreads = Math.max(2, Integer.parseInt(configuredNumThreads));
        } catch (Exception ignored) {
            Runtime runtime = Runtime.getRuntime();
            numThreads = Math.max(2, runtime.availableProcessors());
        }
        final ForkJoinPool.ForkJoinWorkerThreadFactory threadFactory = new ForkJoinPool.ForkJoinWorkerThreadFactory() {
            @Override
            public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                final ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("JUnit-" + thread.getName());
                return thread;
            }
        };
        return new ForkJoinPool(numThreads, threadFactory, null, false);
    }

    private final Deque<ForkJoinTask<?>> _asyncTasks = new LinkedList<ForkJoinTask<?>>();
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
        // Remember scheduled child ...
        _lastScheduledChild = childStatement;
    }

    @Override
    public void finished() {
        final MultiException me = new MultiException();
        if (_lastScheduledChild != null) {
            if (inForkJoinPool()) {
                // Execute the last scheduled child in the current thread ...
                try { _lastScheduledChild.run(); } catch (Throwable t) { me.add(t); }
            } else {
                // Submit the last scheduled child to the ForkJoinPool too ...
                _asyncTasks.addFirst(FORK_JOIN_POOL.submit(_lastScheduledChild));
            }
            // Make sure all asynchronously executed children are done ...
            // Note: Because we have added all tasks via addFirst into _asyncTasks,
            // task.join() is able to steal tasks, which have not been started yet,
            // from other threads ...
            for (ForkJoinTask<?> task : _asyncTasks) {
                try { task.join(); } catch (Throwable t) { me.add(t); }
            }
            me.throwRuntimeExceptionIfNotEmpty();
        }
    }
}
