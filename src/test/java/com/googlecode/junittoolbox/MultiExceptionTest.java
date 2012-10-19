package com.googlecode.junittoolbox;

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
        final MultiException me = new MultiException();
        try { f(); } catch (IOException e) { me.add(e); }
        try { g(); } catch (SQLException e) { me.add(e); }
        return me;
    }

    @Test
    public void test_getMessage() {
        final MultiException me = setUpMultiExceptionWithTwoNestedExceptions();
        assertThat(me.getMessage(), allOf(
            containsString("IOException: foo"),
            containsString("SQLException: bar")
        ));
    }

    @Test
    public void test_getMessage_with_nested_exception() {
        final Exception e1 = new IOException("foo");
        final Exception e2 = new ExecutionException(e1);
        final MultiException me = new MultiException();
        me.add(e2);
        assertThat(me.getMessage(), allOf(
            containsString("java.io.IOException: foo"),
            containsString("MultiExceptionTest.java:42")
        ));
    }

    @Test
    public void test_printStackTrace() {
        final MultiException me = setUpMultiExceptionWithTwoNestedExceptions();
        final StringWriter sw = new StringWriter();
        me.printStackTrace(new PrintWriter(sw));
        assertThat(sw.toString(), allOf(
            containsString("2 nested exceptions:"),
            containsString("IOException: foo"),
            containsString("at com.googlecode.junittoolbox.MultiExceptionTest.f(MultiExceptionTest.java:17)"),
            containsString("SQLException: bar"),
            containsString("at com.googlecode.junittoolbox.MultiExceptionTest.g(MultiExceptionTest.java:21)")
        ));
    }

    @Test
    public void test_printStackTrace_after_throwRuntimeExceptionIfNotEmpty() {
        final MultiException me = setUpMultiExceptionWithTwoNestedExceptions();
        try {
            me.throwRuntimeExceptionIfNotEmpty();
            fail("RuntimeException expected.");
        } catch (RuntimeException e) {
            final StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            assertThat(sw.toString(), allOf(
                containsString("2 nested exceptions:"),
                containsString("IOException: foo"),
                containsString("at com.googlecode.junittoolbox.MultiExceptionTest.f(MultiExceptionTest.java:17)"),
                containsString("SQLException: bar"),
                containsString("at com.googlecode.junittoolbox.MultiExceptionTest.g(MultiExceptionTest.java:21)")
            ));
        }
    }
}
