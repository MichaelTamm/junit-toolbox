package com.googlecode.junittoolbox.util;

import com.googlecode.junittoolbox.MultithreadingTester;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

public class MultiExceptionTest {

    private void f() throws IOException {
        throw new IOException("foo");
    }

    private void g() throws SQLException {
        throw new SQLException("bar");
    }

    private MultiException setUpMultiExceptionWithTwoNestedExceptions() {
        MultiException me = new MultiException();
        try { f(); } catch (IOException e) { me.add(e); }
        try { g(); } catch (SQLException e) { me.add(e); }
        return me;
    }

    @Test
    public void test_getMessage() {
        MultiException me = setUpMultiExceptionWithTwoNestedExceptions();
        assertThat(me.getMessage(), allOf(
            containsString("IOException: foo"),
            containsString("SQLException: bar")
        ));
    }

    @Test
    public void test_getMessage_with_nested_exception() {
        Exception e1 = new IOException("foo");
        Exception e2 = new ExecutionException(e1);
        MultiException me = new MultiException();
        me.add(e2);
        assertThat(me.getMessage(), allOf(
            containsString("java.io.IOException: foo"),
            containsString("MultiExceptionTest.java:43")
        ));
    }

    @Test
    public void test_printStackTrace() {
        MultiException me = setUpMultiExceptionWithTwoNestedExceptions();
        StringWriter sw = new StringWriter();
        me.printStackTrace(new PrintWriter(sw));
        assertThat(sw.toString(), allOf(
            containsString("2 nested exceptions:"),
            containsString("IOException: foo"),
            containsString("at com.googlecode.junittoolbox.util.MultiExceptionTest.f(MultiExceptionTest.java:18)"),
            containsString("SQLException: bar"),
            containsString("at com.googlecode.junittoolbox.util.MultiExceptionTest.g(MultiExceptionTest.java:22)")
        ));
    }

    @Test
    public void test_printStackTrace_after_throwIfNotEmpty() {
        MultiException me = setUpMultiExceptionWithTwoNestedExceptions();
        try {
            me.throwIfNotEmpty();
            fail("MultiException expected.");
        } catch (MultiException expected) {
            final StringWriter sw = new StringWriter();
            expected.printStackTrace(new PrintWriter(sw));
            assertThat(sw.toString(), allOf(
                containsString("2 nested exceptions:"),
                containsString("IOException: foo"),
                containsString("at com.googlecode.junittoolbox.util.MultiExceptionTest.f(MultiExceptionTest.java:18)"),
                containsString("SQLException: bar"),
                containsString("at com.googlecode.junittoolbox.util.MultiExceptionTest.g(MultiExceptionTest.java:22)")
            ));
        }
    }

    @Test
    public void test_throwIfEmpty() {
        MultiException me = new MultiException();
        // The following call should not throw an exception ...
        me.throwIfNotEmpty();

        IOException e1 = new IOException("foo");
        me.add(e1);
        try {
            me.throwIfNotEmpty();
            fail("Exception expected");
        } catch (Exception expected) {
            assertSame(e1, expected);
        }

        me.add(new SQLException("bar"));
        try {
            me.throwIfNotEmpty();
            fail("MultiException expected.");
        } catch (MultiException expected) {
            assertSame(me, expected);
        }
    }

    @Test
    public void test_isEmpty() {
        MultiException me = new MultiException();
        assertTrue(me.isEmpty());

        me.add(new Throwable());
        assertFalse(me.isEmpty());
    }

    @Test
    public void test_thread_safety() {
        final MultiException me = new MultiException();
        new MultithreadingTester().add(new Runnable() {
            @Override
            public void run() {
                me.add(new Exception());
            }
        }).run();
        try {
            me.throwIfNotEmpty();
            fail();
        } catch (MultiException expected) {
            assertThat(expected.getMessage(), startsWith("100000 nested exceptions"));
        }
    }
}
