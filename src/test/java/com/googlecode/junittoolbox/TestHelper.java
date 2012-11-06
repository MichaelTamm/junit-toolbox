package com.googlecode.junittoolbox;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.junit.runner.Runner;
import org.junit.runners.Suite;

import java.lang.reflect.Field;
import java.util.List;

import static org.hamcrest.Matchers.hasItem;

public class TestHelper {

    static List<Runner> getChildren(Runner runner) throws Exception {
        Field fRunnersField = Suite.class.getDeclaredField("fRunners");
        fRunnersField.setAccessible(true);
        // noinspection unchecked
        return (List<Runner>) fRunnersField.get(runner);
    }

    static Matcher<Iterable<? super Runner>> hasItemWithTestClass(Class<?> testClass) {
        return hasItem(withTestClass(testClass));
    }

    private static Matcher<Runner> withTestClass(final Class<?> testClass) {
        return new CustomTypeSafeMatcher<Runner>("with test class " + testClass.getName()) {
            @Override
            protected boolean matchesSafely(Runner runner) {
                return testClass.equals(runner.getDescription().getTestClass());
            }
        };
    }
}
