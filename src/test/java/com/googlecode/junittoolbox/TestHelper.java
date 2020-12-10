package com.googlecode.junittoolbox;

import org.hamcrest.CustomMatcher;
import org.hamcrest.Matcher;
import org.junit.runner.Runner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;

import java.lang.reflect.Method;
import java.util.Collection;

import static org.hamcrest.Matchers.hasItem;

public class TestHelper {

    static Collection<?> getChildren(Runner runner) throws Exception {
        Method getFilteredChildren = ParentRunner.class.getDeclaredMethod("getFilteredChildren");
        getFilteredChildren.setAccessible(true);
        return (Collection<?>) getFilteredChildren.invoke(runner);
    }

    static Matcher<Iterable<?>> hasItemWithTestClass(Class<?> testClass) {
        // noinspection unchecked
        return hasItem(withTestClass(testClass));
    }

    static Matcher hasTestClass(final Class<?> testClass) {
        return new CustomMatcher("has test class " + testClass.getName()) {
            @Override
            public boolean matches(Object item) {
                return item instanceof Runner
                        && testClass.equals(((Runner) item).getDescription().getTestClass());
            }
        };
    }

    private static Matcher withTestClass(final Class<?> testClass) {
        return new CustomMatcher("with test class " + testClass.getName()) {
            @Override
            public boolean matches(Object item) {
                return item instanceof Runner
                       && testClass.equals(((Runner) item).getDescription().getTestClass());
            }
        };
    }

    static Matcher<Iterable<?>> hasItemWithTestMethod(String methodName) {
        // noinspection unchecked
        return hasItem(withTestMethod(methodName));
    }

    private static Matcher withTestMethod(final String methodName) {
        return new CustomMatcher("with test method " + methodName) {
            @Override
            public boolean matches(Object item) {
                return item instanceof FrameworkMethod
                       && methodName.equals(((FrameworkMethod) item).getName());
            }
        };
    }
}
