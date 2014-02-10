package com.googlecode.junittoolbox;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PollingWaitTest {

    @Test
    public void test_immediate_success() throws Exception {
        RunnableAssert runnableAssert = mock(RunnableAssert.class);
        PollingWait wait = spy(new PollingWait());
        wait.pollEvery(5, MILLISECONDS).until(runnableAssert);
        verify(wait, never()).sleep(anyLong());
    }

    @Test
    public void test_success_on_second_try() throws Exception {
        RunnableAssert runnableAssert = mock(RunnableAssert.class);
        doThrow(new Exception("foo")).
        doNothing().
        when(runnableAssert).run();
        PollingWait wait = spy(new PollingWait());
        wait.pollEvery(5, MILLISECONDS).until(runnableAssert);
        verify(wait).sleep(anyLong());
    }

    @Test
    public void test_success_on_third_try() throws Exception {
        RunnableAssert runnableAssert = mock(RunnableAssert.class);
        doThrow(new Exception("foo")).
        doThrow(new Exception("bar")).
        doNothing().
        when(runnableAssert).run();
        PollingWait wait = spy(new PollingWait());
        wait.pollEvery(5, MILLISECONDS).until(runnableAssert);
        verify(wait, times(2)).sleep(anyLong());
    }

    @Test
    public void test_success_on_second_try_if_assert_takes_longer_than_timeout() throws Exception {
        RunnableAssert runnableAssert = mock(RunnableAssert.class);
        doAnswer(sleep(50, MILLISECONDS).thenThrow(new Exception("foo"))).
        doNothing().
        when(runnableAssert).run();
        PollingWait wait = spy(new PollingWait());
        wait.pollEvery(5, MILLISECONDS).timeoutAfter(25, MILLISECONDS).until(runnableAssert);
        verify(wait, never()).sleep(anyLong());
    }

    @Test
    public void test_TimeoutException_message() throws Exception {
        RunnableAssert runnableAssert = mock(RunnableAssert.class);
        doThrow(new Exception("foo")).
        doThrow(new Exception("bar")).
        doThrow(new Exception("abc")).
        doThrow(new Exception("xyz")).
        when(runnableAssert).run();
        boolean success = false;
        try {
            new PollingWait().pollEvery(1, MILLISECONDS).timeoutAfter(10, MILLISECONDS).until(runnableAssert);
            success = true;
        } catch (AssertionError expected) {
            assertThat(expected.getMessage(), containsString("foo"));
            assertThat(expected.getMessage(), containsString("bar"));
            assertThat(expected.getMessage(), containsString("xyz"));
        }
        assertFalse(success);
    }

    @Test
    public void test_error_message_when_PollingWait_instance_is_reused() throws Exception {
        RunnableAssert runnableAssert1 = mock(RunnableAssert.class);
        doThrow(new Exception("foo")).
        doNothing().
        when(runnableAssert1).run();
        RunnableAssert runnableAssert2 = mock(RunnableAssert.class);
        doThrow(new Exception("bar")).
        when(runnableAssert2).run();
        PollingWait wait = spy(new PollingWait());
        wait.pollEvery(1, MILLISECONDS).timeoutAfter(1, MILLISECONDS);
        wait.until(runnableAssert1);
        boolean assertionErrorCaught = false;
        try {
            wait.until(runnableAssert2);
        } catch (AssertionError expected) {
            assertionErrorCaught = true;
            assertThat(expected.getMessage(), containsString("bar"));
            assertThat(expected.getMessage(), not(containsString("foo")));
        }
        assertTrue(assertionErrorCaught);
    }

    private SleepAnswerBuilder sleep(long timeAmount, TimeUnit timeUnit) {
        return new SleepAnswerBuilder(timeUnit.toMillis(timeAmount));
    }

    private static class SleepAnswerBuilder {
        private final long sleepMillis;

        private SleepAnswerBuilder(long sleepMillis) {
            this.sleepMillis = sleepMillis;
        }

        Answer thenThrow(final Exception e) {
            return new Answer() {
                @Override
                public Object answer(InvocationOnMock invocation) throws Throwable {
                    Thread.sleep(sleepMillis);
                    throw e;
                }
            };
        }
    }
}
