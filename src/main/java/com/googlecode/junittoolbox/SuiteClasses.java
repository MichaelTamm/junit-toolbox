package com.googlecode.junittoolbox;

import java.lang.annotation.*;

/**
 * This annotation can be used with the {@link Suite} and {@link ParallelSuite} runner.
 * It allows you to specify the children classes of a test suite class with a
 * <a href="http://ant.apache.org/manual/dirtasks.html#patterns">wildcard pattern</a>.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface SuiteClasses {
    /**
     * <a href="http://ant.apache.org/manual/dirtasks.html#patterns">Wildcard pattern</a>
     * relative to the directory containing the actual test suite class
     * annotated with <code>@RunWith(Suite&#46;class)</code> or
     * <code>@RunWith(ParallelSuite&#46;class)</code>,
     * <strong>must not</strong> start with a <code>'/'</code> character,
     * and <strong>must</strong> end with <code>"&#46;class"</code>.
     */
    public String value();
}
