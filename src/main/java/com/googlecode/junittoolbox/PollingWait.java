package com.googlecode.junittoolbox;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Helper class to wait for asynchronous operations.
 * Usage example:<pre>
 *     private PollingWait wait = new PollingWait().timeoutAfter(5, SECONDS)
 *                                                 .pollEvery(100, MILLISECONDS);
 *     &#64;Test
 *     public void test_auto_complete() throws Exception {
 *         // Enter "cheese" into auto complete field ...
 *         ...
 *         wait.until(new {@link RunnableAssert}("'cheesecake' is displayed in auto-complete &lt;div>") {
 *             &#64;Override
 *             public void run() throws Exception {
 *                 WebElement autoCompleteDiv = driver.findElement(By.id("auto-complete"));
 *                 assertThat(autoCompleteDiv, isVisible());
 *                 assertThat(autoCompleteDiv, containsText("cheesecake"));
 *             }
 *         });
 *     }
 * </pre>
 */
public class PollingWait {

    private long timeoutMillis = 30000;
    private long pollIntervalMillis = 50;
    private final List<Throwable> errors = new ArrayList<Throwable>();

    /**
     * Default: 30 seconds.
     */
    public PollingWait timeoutAfter(long timeAmount, @Nonnull TimeUnit timeUnit) {
        if (timeAmount <= 0) {
            throw new IllegalArgumentException("Invalid timeAmount: " + timeAmount + " -- must be greater than 0");
        }
        timeoutMillis = timeUnit.toMillis(timeAmount);
        return this;
    }

    /**
     * Default: 50 milliseconds.
     */
    public PollingWait pollEvery(long timeAmount, @Nonnull TimeUnit timeUnit) {
        if (timeAmount <= 0) {
            throw new IllegalArgumentException("Invalid timeAmount: " + timeAmount + " -- must be greater than 0");
        }
        pollIntervalMillis = timeUnit.toMillis(timeAmount);
        return this;
    }

    /**
     * Repetitively executes the given <code>runnableAssert</code>
     * until it succeeds without throwing an {@link Error} or
     * {@link Exception} or until the configured {@link #timeoutAfter timeout}
     * is reached, in which case an {@link AssertionError} will be thrown.
     * Calls {@link Thread#sleep} before each retry using the configured
     * {@link #pollEvery interval} to free the CPU for other threads/processes.
     */
    public void until(@Nonnull RunnableAssert runnableAssert) {
        long startTime = System.nanoTime();
        long timeoutReached = startTime + timeoutMillis * 1000000;
        boolean success = false;
        do {
            try {
                runnableAssert.run();
                success = true;
            } catch (Throwable t) {
                if (errors.size() > 0 && startTime > timeoutReached) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append(runnableAssert);
                    sb.append(" did not succeed within ");
                    appendNiceDuration(sb, timeoutMillis);
                    appendErrors(sb, t);
                    throw new AssertionError(sb.toString());
                }
                if (errors.size() < 2) {
                    errors.add(t);
                }
                long sleepTime = pollIntervalMillis - (System.nanoTime() - startTime) / 1000000;
                if (sleepTime > 0) {
                    sleep(pollIntervalMillis);
                }
                startTime = System.nanoTime();
            }
        } while (!success);
    }

    private void appendNiceDuration(StringBuilder sb, long millis) {
        if (millis % 60000 == 0 && millis > 60000) {
            sb.append(millis / 60000).append(" minutes");
            return;
        }
        if (millis % 1000 == 0 && millis > 1000) {
            sb.append(millis / 1000).append(" seconds");
            return;
        }
        sb.append(millis).append(" ms");
    }

    private static final String EXCEPTION_SEPARATOR = "\n\t______________________________________________________________________\n";

    private void appendErrors(StringBuilder sb, Throwable lastError) {
        sb.append(EXCEPTION_SEPARATOR);
        sb.append("\t1st error: ");
        StringWriter sw = new StringWriter();
        errors.get(0).printStackTrace(new PrintWriter(sw));
        sb.append(sw.toString().replace("\n", "\n\t").trim());
        if (errors.size() >= 2) {
            sb.append(EXCEPTION_SEPARATOR);
            sb.append("\t2nd error: ");
            sw = new StringWriter();
            errors.get(1).printStackTrace(new PrintWriter(sw));
            sb.append(sw.toString().replace("\n", "\n\t").trim());
        }
        sb.append(EXCEPTION_SEPARATOR);
        sb.append("\tlast error: ");
        sw = new StringWriter();
        lastError.printStackTrace(new PrintWriter(sw));
        sb.append(sw.toString().replace("\n", "\n\t").trim());
        sb.append(EXCEPTION_SEPARATOR);
    }

    /**
     * Internal method, package private for testing.
     */
    void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Got interrupted.", e);
        }
    }
}
